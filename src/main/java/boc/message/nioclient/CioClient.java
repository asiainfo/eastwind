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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import boc.message.CioCodec;
import boc.message.common.KryoFactory;
import boc.message.common.RequestFuture;
import boc.message.common.RequestFuturePool;
import boc.message.common.RequestInvocationHandler;
import boc.message.common.SharedScheduledExecutor;
import boc.message.common.SubmitRequest;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CioClient {

	private String app;

	private Bootstrap bootstrap;
	private ChannelGuard channelGuard;

	private int threads = 0;
	private int invokeTimeout = 10;
	private int aliveTimeout = 0;

	private Map<String, ClientHandshaker> clientHandshakers = Maps.newHashMap();
	private Map<String, List<Object>> appProviders = Maps.newHashMap();

	private RequestFuturePool requestFuturePool = new RequestFuturePool();
	private RequestInvocationHandler requestInvocationHandler = new RequestInvocationHandler(new NioSubmitRequest());

	public CioClient(String app) {
		this.app = app;
	}

	public void start() {

		bootstrap = new Bootstrap();
		bootstrap.group(new NioEventLoopGroup(threads)).channel(NioSocketChannel.class);
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel sc) throws Exception {
				if (aliveTimeout > 0) {
					sc.pipeline().addLast("readTimeoutHandler", new ReadTimeoutHandler(aliveTimeout));
				}
				sc.pipeline().addLast("lengthFieldPrepender", new LengthFieldPrepender(2, true));
				sc.pipeline().addLast("lengthDecoder", new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));
				sc.pipeline().addLast("codec", new CioCodec(app, KryoFactory.getKryo()));
				sc.pipeline().addLast(new ClientHandshakeHandler(clientHandshakers));
				sc.pipeline().addLast(new ClientInboundHandler(requestFuturePool, channelGuard));
			}
		});

		channelGuard = new ChannelGuard(bootstrap, aliveTimeout);
		channelGuard.start();

		TimeoutRunner timeoutRunner = new TimeoutRunner(requestFuturePool, invokeTimeout);
		SharedScheduledExecutor.ses.scheduleWithFixedDelay(timeoutRunner, 1, 1, TimeUnit.SECONDS);
	}

	@SuppressWarnings("unchecked")
	public <T> T buildProvider(String app, Class<T> interf) {
		synchronized (appProviders) {
			List<Object> providers = appProviders.get(app);
			if (providers == null) {
				providers = Lists.newArrayList();
				appProviders.put(app, providers);
			}
			for (Object p : providers) {
				if (interf.isAssignableFrom(p.getClass())) {
					return (T) p;
				}
			}
			Object obj = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[] { interf },
					requestInvocationHandler);
			providers.add(obj);
			return (T) obj;
		}
	}

	public void addHandshaker(ClientHandshaker handshaker) {
		clientHandshakers.put(handshaker.getName(), handshaker);
	}

	public ChannelGuard getChannelGuard() {
		return channelGuard;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	public int getAliveTimeout() {
		return aliveTimeout;
	}

	public void setAliveTimeout(int aliveTimeout) {
		this.aliveTimeout = aliveTimeout;
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

			ChannelFuture channelFuture = channelGuard.getOrConnect(requestFuture.getApp(), requestFuture.getHost());

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
