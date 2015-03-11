package boc.message.nioclient;

import io.netty.bootstrap.Bootstrap;
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
import boc.message.common.HelloProvider;
import boc.message.common.Host;
import boc.message.common.KryoFactory;
import boc.message.common.NoticeHandlerManager;
import boc.message.common.RequestFuture;
import boc.message.common.RequestFuturePool;
import boc.message.common.RequestInvocationHandler;
import boc.message.common.SharedScheduledExecutor;
import boc.message.common.SubmitRequest;

public class CioClient {

	private String app;

	private Bootstrap bootstrap;
	private ChannelGuard channelGuard;

	private int threads = 0;
	private int invokeTimeout = 10;
	private int channelTimeout = 15;

	private RequestFuturePool requestFuturePool = new RequestFuturePool();
	private RequestInvocationHandler requestInvocationHandler = new RequestInvocationHandler(new NioSubmitRequest());
	private NoticeHandlerManager noticeHandlerManager = new NoticeHandlerManager();

	private HelloProvider helloProvider;

	public CioClient(String app) {
		this.app = app;
		helloProvider = createInvoker(HelloProvider.class);
	}

	public void start() {

		bootstrap = new Bootstrap();
		bootstrap.group(new NioEventLoopGroup(threads)).channel(NioSocketChannel.class);
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel sc) throws Exception {
				if (channelTimeout > 0) {
					sc.pipeline().addLast("readTimeoutHandler", new ReadTimeoutHandler(channelTimeout));
				}
				sc.pipeline().addLast("lengthFieldPrepender", new LengthFieldPrepender(2, true));
				sc.pipeline().addLast("lengthDecoder", new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));
				sc.pipeline().addLast("codec", new CioCodec(app, KryoFactory.getKryo()));
				sc.pipeline().addLast("inboundHandler",
						new ClientInboundHandler(requestFuturePool, channelGuard, noticeHandlerManager));
			}
		});

		channelGuard = new ChannelGuard(bootstrap, channelTimeout);
		channelGuard.start();

		TimeoutRunner timeoutRunner = new TimeoutRunner(requestFuturePool, invokeTimeout);
		SharedScheduledExecutor.ses.scheduleWithFixedDelay(timeoutRunner, 1, 1, TimeUnit.SECONDS);
	}

	public <T> T createInvoker(Class<T> interf) {
		Object obj = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[] { interf },
				requestInvocationHandler);
		return (T) obj;
	}

	public HelloInvoker createCioInvoker(Host host) {
		HelloInvoker invoker = new HelloInvoker(host, helloProvider);
		return invoker;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	public void setChannelTimeout(int ChannelTimeout) {
		this.channelTimeout = ChannelTimeout;
	}

	public int getChannelTimeout() {
		return channelTimeout;
	}

	public int getInvokeTimeout() {
		return invokeTimeout;
	}

	public void setInvokeTimeout(int invokeTimeout) {
		this.invokeTimeout = invokeTimeout;
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
