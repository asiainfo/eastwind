package boc.message.nioclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

import boc.message.CioCodec;
import boc.message.common.CioProvider;
import boc.message.common.Host;
import boc.message.common.KryoFactory;
import boc.message.common.NoticeHandlerManager;
import boc.message.common.Ping;
import boc.message.common.RequestFuture;
import boc.message.common.RequestFuturePool;
import boc.message.common.RequestInvokeHandler;
import boc.message.common.SharedScheduledExecutor;
import boc.message.common.SubmitRequest;

public class CioClient {

	private String app;

	private Bootstrap bootstrap;
	private ChannelGuard channelGuard;

	private int threads = 0;
	private int invokeTimeout = 10;
	private int ChannelTimeout = 15;

	private RequestFuturePool requestFuturePool = new RequestFuturePool();
	private RequestInvokeHandler requestInvokeHandler = new RequestInvokeHandler(new NioSubmitRequest());
	private NoticeHandlerManager noticeHandlerManager = new NoticeHandlerManager();

	private CioProvider cioProvider;

	public CioClient(String app) {
		this.app = app;
		cioProvider = createInvoker(CioProvider.class);
	}

	public void start() {

		bootstrap = new Bootstrap();
		bootstrap.group(new NioEventLoopGroup(threads)).channel(NioSocketChannel.class);
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel sc) throws Exception {
				sc.pipeline().addLast("readTimeoutHandler", new ReadTimeoutHandler(ChannelTimeout));
				sc.pipeline().addLast("lengthFieldPrepender", new LengthFieldPrepender(2, true));
				sc.pipeline().addLast("lengthDecoder", new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));
				sc.pipeline().addLast("codec", new CioCodec(app, KryoFactory.getKryo()));
				sc.pipeline().addLast("inboundHandler",
						new ClientInboundHandler(requestFuturePool, channelGuard, noticeHandlerManager));
			}
		});

		channelGuard = new ChannelGuard(bootstrap, this);
		channelGuard.start();

		TimeoutRunner timeoutRunner = new TimeoutRunner(requestFuturePool, invokeTimeout);
		SharedScheduledExecutor.ses.scheduleWithFixedDelay(timeoutRunner, 1, 1, TimeUnit.SECONDS);
	}

	public <T> T createInvoker(Class<T> interf) {
		Object obj = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[] { interf },
				requestInvokeHandler);
		return (T) obj;
	}

	public CioInvoker buildCioInvoker(Host host) {
		CioInvoker builder = new CioInvoker(host, cioProvider);
		return builder;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	public void setChannelTimeout(int ChannelTimeout) {
		this.ChannelTimeout = ChannelTimeout;
	}

	public int getChannelTimeout() {
		return ChannelTimeout;
	}

	public int getInvokeTimeout() {
		return invokeTimeout;
	}

	public void setInvokeTimeout(int invokeTimeout) {
		this.invokeTimeout = invokeTimeout;
	}

	public void ping(Host host) {
		Channel channel = channelGuard.getChannel(host);
		if (channel != null) {
			channel.writeAndFlush(Ping.instance);
		}
	}

	private class NioSubmitRequest implements SubmitRequest {

		@Override
		public void submit(final RequestFuture<?> requestFuture) {
			if (channelGuard.isShutdowning(requestFuture.getHost())) {
				requestFuture.fail();
				return;
			}

			ChannelFuture channelFuture = channelGuard.getChannelOrConnect(requestFuture.getHost());

			if (channelFuture.isSuccess()) {
				requestFuturePool.put(requestFuture);
				channelFuture.channel().writeAndFlush(requestFuture.getRequest());
			} else if (channelFuture.cause() != null) {
				requestFuture.fail();
			} else {
				channelFuture.addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						if (future.isSuccess()) {
							requestFuturePool.put(requestFuture);
							future.channel().writeAndFlush(requestFuture.getRequest());
						} else if (future.cause() != null) {
							requestFuture.fail();
						}
					}
				});
			}
		}

	}

}
