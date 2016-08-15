package eastwind.io3;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eastwind.io.common.Host;

public class RemoteAppManager {

	private App app;
	private PromiseSustainer promiseSustainer;

	private AtomicInteger configId = new AtomicInteger();

	private Map<String, RemoteAppGroup> groups = Maps.newHashMap();

	private WeakHashMap<Channel, Transport> transports = new WeakHashMap<Channel, Transport>();
	private Map<Host, List<ServerTransport>> serverTransports = Maps.newHashMap();

	private Bootstrap bootstrap;

	public RemoteAppManager(App app, PromiseSustainer promiseSustainer, Bootstrap bootstrap) {
		this.app = app;
		this.promiseSustainer = promiseSustainer;
		this.bootstrap = bootstrap;
	}

	public RemoteAppGroup getGroup(String group) {
		RemoteAppGroup ag = groups.get(group);
		if (ag != null) {
			return ag;
		}
		synchronized (groups) {
			ag = groups.get(group);
			if (ag == null) {
				ag = new RemoteAppGroup(group);
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
		RemoteConfig rc = new RemoteConfig(configId.getAndIncrement(), null, host);
		return connect(rc, 0);
	}

	public Handshake access(Handshake hs, Channel c) {
		RemoteAppGroup g = getGroup(hs.getGroup());
		RemoteApp ra = g.get(hs.getMyUuid());
		Transport t = new Transport(ra.getGroup(), promiseSustainer);
		t.setChannel(c);
		t.handshake(hs, ra);
		ra.addTransport(t);
		transports.put(c, t);
		Handshake back = handshakeObj();
		return back;
	}

	public Transport getTransport(Channel channel) {
		return transports.get(channel);
	}

	private Handshake handshakeObj() {
		return new Handshake(app.getGroup(), app.getUuid(), null);
	}

	public void setDepended(String group) {
		RemoteAppGroup ag = getGroup(group);
		if (!ag.isDependent()) {
			ag.setDependent(true);
			for (RemoteConfig rc : ag.getConfigs()) {
				connect(rc, 0);
			}
		}
	}

	public void addConfig(String group, Host host) {
		RemoteConfig sc = new RemoteConfig(configId.getAndIncrement(), group, host);
		RemoteAppGroup ag = getGroup(group);
		ag.addConfig(sc);
		if (ag.isDependent()) {
			connect(sc, 0);
		}
	}

	public void connectNow(ServerTransport st) {
		connect0(st, 0);
	}

	private static final class InnerInvocationListener implements OperationListener<TransmitPromise> {
		private final InvocationPromise ip;

		private InnerInvocationListener(InvocationPromise ip) {
			this.ip = ip;
		}

		@Override
		public void complete(TransmitPromise tp) {
			if (tp.getTh() != null) {
				ip.setException(tp.getTh());
			} else {
				ip.set(tp.getNow());
			}
		}
	}

	public class RemoteInvocationHandler implements InvocationHandler {

		private String group;

		public RemoteInvocationHandler(String group) {
			this.group = group;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			InvocationMode mode = InvocationMode.TL.get();
			if (mode == null) {
				mode = InvocationMode.DEFAULT;
			} else {
				InvocationMode.TL.set(null);
			}
			mode.setHost(new Host("127.0.0.1", 12468));
			Host host = mode.getHost();
			if (host != null) {
				return handleSingInvoke(mode, method, args);
			}
			return handleGroupInvoke(mode, method, args);
		}

		private Object returnNull(Method method) {
			Class<?> type = method.getReturnType();
			// boolean, char, byte, short, int, long, float, double
			if (type.isPrimitive()) {
				if (type == boolean.class) {
					return false;
				}

				if (type == int.class) {
					return Integer.MIN_VALUE;
				}
				if (type == long.class) {
					return Long.MIN_VALUE;
				}

				if (type == byte.class) {
					return Byte.MIN_VALUE;
				}
				if (type == short.class) {
					return Short.MIN_VALUE;
				}
				if (type == float.class) {
					return Float.MIN_VALUE;
				}
				if (type == double.class) {
					return Double.MIN_VALUE;
				}

				if (type == char.class) {
					return (char) 0xffff;
				}
			} else {
				return null;
			}
			return null;
		}

		private Object handleGroupInvoke(final InvocationMode mode, final Method method, final Object[] args) {
			
			RemoteAppGroup g = groups.get(group);
			if (g == null) {
				// throw
			}
			if (!g.isDependent()) {
				setDepended(g.getGroup());
			}
			
			
			
			return null;
		}

		private Object handleSingInvoke(final InvocationMode mode, final Method method, final Object[] args)
				throws InterruptedException, ExecutionException {
			Host host = mode.getHost();
			RemoteAppGroup g = groups.get(group);
			if (g == null) {
				// throw
			}
			if (!g.isDependent()) {
				setDepended(g.getGroup());
			}

			final InvocationPromise ip = new InvocationPromise();
			final ServerTransport st = null;
			if (st == null) {
				// throw
			}
			boolean handled = false;
			if (st.getState() == Transport.CONNECTING || st.getState() == Transport.HANDSHAKE) {
				st.addHandshakeListener(new OperationListener<ServerTransport>() {
					@Override
					public void complete(ServerTransport t) {
						acceptAndSend(mode, method, args, ip, st);
					}
				});
				handled = true;
			}
			if (st.isReady() && !handled) {
				acceptAndSend(mode, method, args, ip, st);
			}
			if (mode.isSync()) {
				return ip.get();
			} else {
				return returnNull(method);
			}
		}

		private void acceptAndSend(InvocationMode mode, Method method, final Object[] args,
				final InvocationPromise ip, final ServerTransport st) {
			ListenablePromise<String> lp = st.acceptable(method);
			if (lp.isDone()) {
				if (lp.getNow() == null) {
					throw new RuntimeException("unaccepted invocation");
				} else {
					send(args, ip, st, lp);
				}
			} else {
				lp.addListener(new OperationListener<ListenablePromise<String>>() {
					@Override
					public void complete(ListenablePromise<String> lp) {
						if (lp.isDone() && !lp.isCancelled() && lp.getNow() != null) {
							send(args, ip, st, lp);
						}
					}
				});
			}
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private void send(final Object[] args, final InvocationPromise ip, final ServerTransport st,
				final ListenablePromise<String> lp) {
			Request r = new Request(lp.getNow(), args);
			TransmitPromise tp = st.send(r, null);
			tp.addListener(new InnerInvocationListener(ip));
			GlobalExecutor.DELAYED_EXECUTOR.submit(TransmitTicker.class, tp, 1000);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T createInvoker(String group, Class<?> interf) {
		return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] { interf },
				new RemoteInvocationHandler(group));
	}

	private ServerTransport connect(RemoteConfig rc, int n) {
		ServerTransport st = getTransport(rc.getHost(), rc.getId());
		if (st == null) {
			st = new ServerTransport(rc, promiseSustainer, this);
			List<ServerTransport> l = serverTransports.get(rc.getHost());
			if (l == null) {
				l = Lists.newLinkedList();
				serverTransports.put(rc.getHost(), l);
			}
			l.add(st);
			if (!StringUtils.isBlank(rc.getGroup())) {
				RemoteAppGroup group = getGroup(rc.getGroup());
//				group.add(st);
			}
		}
		connect0(st, 0);
		return st;
	}

	private void connect0(ServerTransport st, int n) {
		if (st.acquire()) {
			Channel c = st.getChannel();
			if (c != null) {
				transports.remove(c);
			}
			st.reset();
			Host host = st.getRemoteConfig().getHost();
			ChannelFuture cf = bootstrap.connect(host.getIp(), host.getPort());
			c = cf.channel();
			st.setChannel(c);
			transports.put(c, st);
			c.closeFuture().addListener(new ConnectListener(st, n));
			cf.addListener(new HandshakeListener(st));
		}
	}

	private ServerTransport getTransport(Host host, int id) {
		List<ServerTransport> transports = serverTransports.get(host);
		if (transports == null) {
			return null;
		}
		for (ServerTransport st : transports) {
			if (st.getRemoteConfig().getId() == id) {
				return st;
			}
		}
		return null;
	}

	private class ConnectListener implements GenericFutureListener<ChannelFuture> {

		private ServerTransport st;
		private int n;

		public ConnectListener(ServerTransport st, int n) {
			this.st = st;
			this.n = n;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (st.getChannel() == future.channel()) {
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

		private ServerTransport st;

		private HandshakeListener(ServerTransport st) {
			this.st = st;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (future.isSuccess()) {
				st.setState(Transport.HANDSHAKE);
				Handshake hs = handshakeObj();
				UniqueObject uo = new UniqueObject();
				uo.setCall(true);
				uo.setObj(hs);

				@SuppressWarnings("unchecked")
				ListenablePromise<Handshake> lp = st.send(uo, this);
				lp.addListener(new OperationListener<ListenablePromise<Handshake>>() {
					@Override
					public void complete(ListenablePromise<Handshake> lp) {
						Handshake hs = lp.getNow();
						RemoteAppGroup g = getGroup(hs.getGroup());
						RemoteApp a = g.get(hs.getYourUuid());
						a.addServerTransport(st);
						st.handshake(hs, g.get(hs.getMyUuid()));
					}
				});
			}
		}
	}

}
