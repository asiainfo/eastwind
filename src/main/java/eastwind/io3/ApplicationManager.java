package eastwind.io3;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eastwind.io.common.Host;

public class ApplicationManager {

	private Application application;
	private TransportSustainer transportSustainer;

	private AtomicInteger configId = new AtomicInteger();

	private Map<String, GroupConfig> groupConfigs = Maps.newHashMap();
	private Map<String, ApplicationGroup> groups = Maps.newHashMap();
	
	private WeakHashMap<Channel, Transport> transports = new WeakHashMap<Channel, Transport>();
	private Map<Host, List<ServerTransport>> serverTransports = Maps.newHashMap();

	private Bootstrap bootstrap;

	public ApplicationManager(TransportSustainer transportSustainer, Bootstrap bootstrap) {
		this.transportSustainer = transportSustainer;
		this.bootstrap = bootstrap;
	}

	public ApplicationGroup getGroup(String group) {
		ApplicationGroup ag = groups.get(group);
		if (ag != null) {
			return ag;
		}
		synchronized (groups) {
			ag = groups.get(group);
			if (ag == null) {
				ag = new ApplicationGroup(group);
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
		ApplicationGroup group = getGroup(hs.getGroup());
		RemoteApplication ra = group.get(hs.getMyUuid());
		Transport transport = new Transport(ra.getGroup(), transportSustainer);
		transport.setChannel(channel);
		transport.handshake(hs, ra);
		ra.addTransport(transport);
		transports.put(channel, transport);
		Handshake back = handshakeObj();
		return back;
	}

	public Transport getTransport(Channel channel) {
		return transports.get(channel);
	}
	
	private Handshake handshakeObj() {
		Handshake hs = new Handshake();
		hs.setGroup(application.getGroup());
		hs.setMyUuid(application.getUuid());
		return hs;
	}

	public void depend(String group) {
		GroupConfig gc = getGroupConfig(group);
		gc.dependent = true;
		for (ServerConfig sc : gc.configs) {
			connect(sc, 0);
		}
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

	public class RemoteInvocationHandler implements InvocationHandler {

		private String group;

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			InvocationMode mode = InvocationMode.TL.get();
			if (mode == null) {
				mode = InvocationMode.DEFAULT;
			} else {
				InvocationMode.TL.set(null);
			}
			Host host = mode.getHost();
			if (host != null) {
				handleSingInvoke(mode, method, args);
			}
			return null;
		}

		private void handleSingInvoke(InvocationMode mode, Method method, Object[] args) {
			Host host = mode.getHost();
			GroupConfig gc = getGroupConfig(group);
			if (!gc.dependent) {
				depend(group);
			}
			ApplicationGroup g = groups.get(group);
			ServerTransport st = g.getTransport(host);
			if (st == null) {
				// throw
			}
			if (st.isReady()) {
				ListenablePromise<String> lp = st.acceptable(method);
				if (lp.isDone()) {
					if (lp.getNow() == null) {
						// throw
					} else {
						Request r = new Request();
						r.setNamespace(lp.getNow());
						r.setArgs(args);
						@SuppressWarnings("rawtypes")
						TransportPromise tp = st.publish(r, null);
						
					}
				} else {
					// add listener
				}
			} else {
				
			}
		}
	}
	
	
	private class GroupInvocationHandler implements InvocationHandler {
		private String group;

		@Override
		public Object invoke(Object proxy, final Method method, Object[] args) throws Throwable {
			if (Object.class.equals(method.getDeclaringClass())) {
				return method.invoke(this, args);
			}
			ApplicationGroup g = groups.get(group);
			if (g == null) {
				GroupConfig gc = groupConfigs.get(group);
				if (gc == null || gc.configs.size() == 0) {
					// TODO throw exception
				}
				if (!gc.dependent) {
					depend(group);
				}
			}
			Request request = new Request();
			request.setArgs(args);

			@SuppressWarnings({ "unchecked", "rawtypes" })
			GroupTransportPromise promise = new GroupTransportPromise(request, method, g.getTransports());
			return promise.get();
		}
	}

	private class GroupTransportPromise<V> extends ListenablePromise<V> implements OperationListener<ServerTransport> {

		private Request request;
		private Method method;
		private ServerTransport current;
		private List<ServerTransport> transports;

		public GroupTransportPromise(Request request, Method method, List<ServerTransport> transports) {
			this.request = request;
			this.method = method;
			this.transports = transports;
			for (ServerTransport st : transports) {
				st.addHandshakeListener(this);
			}
		}

		@Override
		public void operationComplete(final ServerTransport st) {
			if (st.getState() == Transport.CONNECT_FAIL) {
				this.transports.remove(st);
				checkValid();
			} else if (st.getState() == Transport.READY) {
				ListenablePromise<String> lp = st.acceptable(method);
				lp.addListener(new OperationListener<ListenablePromise<String>>() {
					@SuppressWarnings("unchecked")
					@Override
					public void operationComplete(ListenablePromise<String> t) {
						if (t.getNow() == null) {
							transports.remove(st);
							checkValid();
						} else {
							if (current == null) {
								current = st;
								@SuppressWarnings("rawtypes")
								TransportPromise tp = current.publish(request, current);
								tp.addListener(new OperationListener<ListenablePromise<V>>() {
									@Override
									public void operationComplete(ListenablePromise<V> lp) {
										@SuppressWarnings("rawtypes")
										TransportPromise tp = (TransportPromise) lp;
										if (tp.getTh() == null) {
											GroupTransportPromise.this.succeeded((V) tp.getNow());
										}
									}
								});
							}
						}
					}
				});
			}
		}

		private void checkValid() {
			if (transports.size() == 0) {
				// TODO
				// super.failed(th);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T createInvoker(String group, Class<?> interf) {
		return null;
	}

	private InvocationHandler handler = new GroupInvocationHandler();

	private ServerTransport connect(ServerConfig conf, int n) {
		ServerTransport st = getTransport(conf.getHost(), conf.getId());
		if (st == null) {
			st = new ServerTransport(conf, transportSustainer, this);
			List<ServerTransport> l = serverTransports.get(conf.getHost());
			if (l == null) {
				l = Lists.newLinkedList();
				serverTransports.put(conf.getHost(), l);
			}
			l.add(st);
			if (!StringUtils.isBlank(conf.getGroup())) {
				ApplicationGroup group = getGroup(conf.getGroup());
				group.add(st);
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
			Host host = st.getServerConfig().getHost();
			ChannelFuture cf = bootstrap.connect(host.getIp(), host.getPort());
			c = cf.channel();
			st.setChannel(c);
			transports.put(c, st);
			c.closeFuture().addListener(new RetryListener(st, n));
			cf.addListener(new HandshakeListener(st));
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

	private class RetryListener implements GenericFutureListener<ChannelFuture> {

		private ServerTransport st;
		private int n;

		public RetryListener(ServerTransport st, int n) {
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
				ListenablePromise<Handshake> lp = st.publish(uo, this);
				lp.addListener(new OperationListener<ListenablePromise<Handshake>>() {
					@Override
					public void operationComplete(ListenablePromise<Handshake> lp) {
						Handshake hs = lp.getNow();
						ApplicationGroup g = getGroup(hs.getGroup());
						RemoteApplication a = g.get(hs.getYourUuid());
						a.addServerTransport(st);
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
