package eastwind.io.nioclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;

import eastwind.io.WindCodec;
import eastwind.io.common.Host;
import eastwind.io.common.InvocationFuturePool;
import eastwind.io.common.KryoFactory;
import eastwind.io.common.ScheduledExecutor;
import eastwind.io.common.TimedIdSequence100;

public class EastWindClient {

	private String app;

	private Bootstrap bootstrap;
	private ChannelGuard channelGuard;

	private int threads = 0;
	private int invokeTimeout = 10;
	private int aliveTimeout = 0;

	private TimedIdSequence100 timedIdSequence100 = new TimedIdSequence100();
	private List<ProviderGroup> providerGroups = Lists.newArrayList();
	private InvocationFuturePool invocationFuturePool = new InvocationFuturePool();

	public EastWindClient(String app) {
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
				sc.pipeline().addLast("windCodec", new WindCodec(app, KryoFactory.getKryo()));
				sc.pipeline().addLast(new ClientHandshakeHandler(app));
				sc.pipeline().addLast(new ClientInboundHandler(invocationFuturePool, channelGuard));
			}
		});

		channelGuard = new ChannelGuard(bootstrap);
		channelGuard.start();

		TimeoutRunner timeoutRunner = new TimeoutRunner(invocationFuturePool, invokeTimeout);
		ScheduledExecutor.ses.scheduleWithFixedDelay(timeoutRunner, 1, 1, TimeUnit.SECONDS);
	}

	public void createProviderGroup(String app, List<Host> hosts, ClientHandshaker clientHandshaker) {
		synchronized (providerGroups) {
			if (getProviderGroup(app) == null) {
				ProviderGroup pg = new ProviderGroup(app, hosts, clientHandshaker);
				providerGroups.add(pg);
				for (Host host : hosts) {
					channelGuard.add(app, host, clientHandshaker);
				}
			}
		}
	}

	public ProviderGroup getProviderGroup(String app) {
		for (int i = 0; i < providerGroups.size(); i++) {
			ProviderGroup pg = providerGroups.get(i);
			if (pg.getApp().equals(app)) {
				return pg;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T> T getProvider(String app, Class<T> interf) {
		ProviderGroup pg = getProviderGroup(app);
		Object provider = pg.getProvider(interf);
		if (provider != null) {
			return (T) provider;
		}
		provider = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[] { interf },
				new InvocationFutureHandler(timedIdSequence100, pg, channelGuard, invocationFuturePool));
		pg.addProvider(interf, provider);
		return (T) provider;
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

}
