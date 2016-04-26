package eastwind.io3;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import com.google.common.collect.Maps;

public class ServerTransport extends Transport {

	private ServerConfig serverConfig;
	private ApplicationManager applicationManager;
	private TransportInvocationHandler handler = new TransportInvocationHandler();

	public ServerTransport(ServerConfig serverConfig, TransportContext transportContext,
			ApplicationManager applicationManager) {
		super(serverConfig.getGroup(), transportContext);
		this.serverConfig = serverConfig;
		this.applicationManager = applicationManager;
	}

	private ListenablePromise<ServerTransport> handshakePromise;

	public void addHandshakeListener(final OperationListener<ServerTransport> listener) {
		handshakePromise.addListener(new OperationListener<ListenablePromise<ServerTransport>>() {
			@Override
			public void operationComplete(ListenablePromise<ServerTransport> t) {
				listener.operationComplete(ServerTransport.this);
			}
		});
	}

	@Override
	protected void handshake0(Handshake hs) {
		handshakePromise.succeeded(this);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void publish0(final Unique message, final TransportPromise tp) {
		Channel channel = getChannel();
		if (channel == null || !channel.isActive() || state == CONNECT_FAIL) {
			retry(message, tp);
			return;
		}
		channel.writeAndFlush(message).addListener(new GenericFutureListener<ChannelFuture>() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (!future.isSuccess()) {
					retry(message, tp);
				}
			}
		});
	}

	@SuppressWarnings("rawtypes")
	private void retry(final Unique message, final TransportPromise tp) {
		applicationManager.connectNow(this);
		tp.setReconnect(true);
		addHandshakeListener(new OperationListener<ServerTransport>() {
			@Override
			public void operationComplete(ServerTransport t) {
				if (state == READY) {
					Channel channel = getChannel();
					tp.setReconnect(false);
					channel.writeAndFlush(message).addListener(new GenericFutureListener<ChannelFuture>() {
						@Override
						public void operationComplete(ChannelFuture future) throws Exception {
							if (!future.isSuccess()) {
								retry(message, tp);
							}
						}
					});
				} else {
					// TODO fail
				}
			}
		});
	}

	public synchronized boolean acquire() {
		if (this.state == NEW || this.state == CONNECT_FAIL) {
			this.state = CONNECTING;
			return true;
		}
		return false;
	}

	private Map<Class<?>, Object> invokers = Maps.newHashMap();
	
	@SuppressWarnings("unchecked")
	public <T> T createInvoker(Class<T> interf) {
		Object obj = invokers.get(interf);
		if (obj == null) {
			obj = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[] { interf }, handler);
			invokers.put(interf, obj);
		}
		return (T) obj;
	}

	private class TransportInvocationHandler implements InvocationHandler {
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (Object.class.equals(method.getDeclaringClass())) {
				return method.invoke(this, args);
			}
			Request request = new Request();
			request.setArgs(args);
			ListenablePromise<String> ns = acceptable(method);
			if (!ns.isDone()) {
				ns.get();
			}
			request.setNamespace(ns.getNow());
			@SuppressWarnings("rawtypes")
			TransportPromise tp = ServerTransport.super.publish(request, this);
			return tp.get();
		}
	}

	@SuppressWarnings("unchecked")
	public ListenablePromise<String> acceptable(Method method) {
		ListenablePromise<String> lp = remoteApplication.acceptable(method);
		if (lp != null) {
			return lp;
		}
		UniqueObject uo = new UniqueObject();
		uo.setCall(true);
		RpcDescriptor rd = new RpcDescriptor();
		rd.setInterf(method.getDeclaringClass().getName());
		rd.setMethod(method.getName());
		Class<?>[] cls = method.getParameterTypes();
		String[] pts = new String[cls.length];
		for (int i = 0; i < cls.length; i++) {
			pts[i] = cls[i].getCanonicalName();
		}
		rd.setParameterTypes(pts);
		uo.setObj(rd);
		lp = super.publish(uo, null);
		remoteApplication.addAcceptMethodPromise(lp);
		return lp;
	}

	public ServerConfig getServerConfig() {
		return serverConfig;
	}

	@Override
	protected void setChannel(Channel channel) {
		super.setChannel(channel);
		this.handshakePromise = new ListenablePromise<ServerTransport>();
	}

}
