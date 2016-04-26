package eastwind.io3;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eastwind.io.common.Host;

public class ApplicationManager {

	private TransportContext transportContext;

	private AtomicInteger configId = new AtomicInteger();

	private Map<String, GroupConfig> groupConfigs = Maps.newHashMap();
	private Map<String, RemoteApplicationGroup> groups = Maps.newHashMap();

	private Map<Host, List<ServerTransport>> serverTransports = Maps.newHashMap();

	private Bootstrap bootstrap;

	public ApplicationManager(TransportContext transportContext, Bootstrap bootstrap) {
		this.transportContext = transportContext;
		this.bootstrap = bootstrap;
	}

	public RemoteApplicationGroup getGroup(String group) {
		RemoteApplicationGroup ag = groups.get(group);
		if (ag != null) {
			return ag;
		}
		synchronized (groups) {
			ag = groups.get(group);
			if (ag == null) {
				ag = new RemoteApplicationGroup(group);
				groups.put(group, ag);
			}
		}
		return ag;
	}

	public ServerTransport getTransport(Host host) {
		List<ServerTransport> l = serverTransports.get(host);
		if (l != null && l.size() > 0) {
			if (l.size() == 1) {
				return l.get(0);
			} else {
				return l.get((int) (System.currentTimeMillis() % l.size()));
			}
		}
		ServerConfig sc = new ServerConfig(configId.getAndIncrement(), null, host);
		return connect(sc, 0);
	}

	public Handshake access(Handshake hs, Channel channel) {
		RemoteApplicationGroup group = getGroup(hs.getGroup());
		RemoteApplication ra = group.get(hs.getMyUuid());
		Transport transport = new Transport(ra.getGroup(), transportContext);
		transport.setChannel(channel);
		transport.handshake(hs, ra);
		ra.addTransport(transport);

		Handshake hs2 = new Handshake();
		hs2.setGroup(transportContext.getLocalApplication().getGroup());
		hs2.setMyUuid(transportContext.getLocalApplication().getUuid());
		return hs2;
	}

	public void depend(String group) {
		getGroupConfig(group).dependent = true;
	}

	public void addConfig(String group, Host host) {
		GroupConfig gc = getGroupConfig(group);
		ServerConfig conf = new ServerConfig(configId.getAndIncrement(), group, host);
		gc.configs.add(conf);
		if (gc.dependent) {
			connect(conf, 0);
		}
	}

	public void connectNow(ServerTransport st) {
		connect0(st, 0);
	}

	private class GroupTransportInvocationHandler implements InvocationHandler {
		private String group;
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (Object.class.equals(method.getDeclaringClass())) {
				return method.invoke(this, args);
			}
			RemoteApplicationGroup g = groups.get(group);
			ServerTransport st = g.next();
			
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T createInvoker(String group, Class<?> interf) {
		return null;
	}
	
	private InvocationHandler handler = new GroupTransportInvocationHandler();
	
	private ServerTransport connect(ServerConfig conf, int n) {
		ServerTransport st = getTransport(conf.getHost(), conf.getId());
		if (st == null) {
			st = new ServerTransport(conf, transportContext, this);
			List<ServerTransport> l = serverTransports.get(conf.getHost());
			if (l == null) {
				l = Lists.newLinkedList();
				serverTransports.put(conf.getHost(), l);
			}
			l.add(st);
		}
		connect0(st, 0);
		return st;
	}

	private void connect0(ServerTransport st, int n) {
		if (st.acquire()) {
			Host host = st.getServerConfig().getHost();
			ChannelFuture cf = bootstrap.connect(host.getIp(), host.getPort());
			st.setChannel(cf.channel());
			cf.channel().closeFuture().addListener(new RetryListener(st, n));
			cf.addListener(new HandshakeListener(transportContext, st));
		}
	}

	private GroupConfig getGroupConfig(String group) {
		GroupConfig gc = groupConfigs.get(group);
		if (gc == null) {
			gc = new GroupConfig();
			groupConfigs.put(group, gc);
		}
		return gc;
	}

	private ServerTransport getTransport(Host host, int id) {
		List<ServerTransport> transports = serverTransports.get(host);
		if (transports == null) {
			return null;
		}
		for (ServerTransport st : transports) {
			if (st.getServerConfig().getId() == id) {
				return st;
			}
		}
		return null;
	}

	class RetryListener implements GenericFutureListener<ChannelFuture> {

		private ServerTransport st;
		private int n;

		public RetryListener(ServerTransport st, int n) {
			this.st = st;
			this.n = n;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (st.getChannel() == future.channel()) {
				System.out.println(n + " closed");
				st.setState(Transport.CONNECT_FAIL);
				if (n == 0) {
					connect0(st, 1);
				} else {
					n++;
					if (n > 8) {
						n = 8;
					}
					GlobalExecutor.SCHEDULED_EXECUTOR.schedule(new Runnable() {
						@Override
						public void run() {
							connect0(st, n);
						}
					}, n, TimeUnit.SECONDS);
				}
			}
		}

	}

	private class HandshakeListener implements GenericFutureListener<ChannelFuture> {

		private TransportContext tc;
		private ServerTransport st;

		private HandshakeListener(TransportContext tc, ServerTransport st) {
			this.tc = tc;
			this.st = st;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (future.isSuccess()) {
				st.setState(Transport.HANDSHAKE);
				Handshake hs = new Handshake();
				hs.setGroup(tc.getLocalApplication().getGroup());
				hs.setMyUuid(tc.getLocalApplication().getUuid());

				UniqueObject uo = new UniqueObject();
				uo.setCall(true);
				uo.setObj(hs);

				@SuppressWarnings("unchecked")
				ListenablePromise<Handshake> lp = st.publish(uo, this);
				lp.addListener(new OperationListener<ListenablePromise<Handshake>>() {
					@Override
					public void operationComplete(ListenablePromise<Handshake> lp) {
						Handshake hs = lp.getNow();
						RemoteApplicationGroup g = getGroup(hs.getGroup());
						RemoteApplication ra = g.get(hs.getYourUuid());
						ra.addServerTransport(st);
						st.handshake(hs, g.get(hs.getMyUuid()));
					}
				});
			}
		}
	}

	private static class GroupConfig {
		volatile boolean dependent;
		List<ServerConfig> configs = Lists.newArrayList();
	}
}
