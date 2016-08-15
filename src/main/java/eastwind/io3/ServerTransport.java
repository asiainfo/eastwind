package eastwind.io3;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;

import java.lang.reflect.Method;

public class ServerTransport extends Transport {

	private RemoteConfig remoteConfig;
	private RemoteAppManager applicationManager;

	public ServerTransport(RemoteConfig remoteConfig, PromiseSustainer promiseSustainer,
			RemoteAppManager applicationManager) {
		super(remoteConfig.getGroup(), promiseSustainer);
		this.remoteConfig = remoteConfig;
		this.applicationManager = applicationManager;
	}

	private ListenablePromise<ServerTransport> handshakePromise;

	public void addHandshakeListener(final OperationListener<ServerTransport> listener) {
		handshakePromise.addListener(new OperationListener<ListenablePromise<ServerTransport>>() {
			@Override
			public void complete(ListenablePromise<ServerTransport> t) {
				listener.complete(ServerTransport.this);
			}
		});
	}

	@Override
	protected void handshake0(Handshake hs) {
		handshakePromise.succeeded(this);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void send0(final TransmitPromise tp) {
		Channel channel = getChannel();
		Object message = tp.getMessage();
		if (isReady()) {
			channel.writeAndFlush(message).addListener(new RetryListener(tp));
			return;
		}
		if (state == HANDSHAKE
				&& (message instanceof UniqueObject && ((UniqueObject) message).getObj() instanceof Handshake)) {
			channel.writeAndFlush(message);
			return;
		}
		if (channel == null || !channel.isActive() || state == CONNECT_FAIL) {
			retry(tp);
			return;
		}
	}

	@SuppressWarnings("rawtypes")
	private void retry(final TransmitPromise tp) {
		applicationManager.connectNow(this);
		tp.setRetry(true);
		addHandshakeListener(new OperationListener<ServerTransport>() {
			@Override
			public void complete(ServerTransport t) {
				Channel channel = getChannel();
				if (t.isReady()) {
					tp.setRetry(false);
					channel.writeAndFlush(tp.getMessage()).addListener(new RetryListener(tp));
				} else {
					// TODO fail
				}
			}
		});
	}

	private class RetryListener implements GenericFutureListener<ChannelFuture> {

		@SuppressWarnings("rawtypes")
		private TransmitPromise tp;

		@SuppressWarnings({ "rawtypes" })
		public RetryListener(TransmitPromise tp) {
			this.tp = tp;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (!future.isSuccess()) {
				retry(tp);
			}
		}

	}

	public boolean acquire() {
		if (this.state == NEW || this.state == CONNECT_FAIL) {
			this.state = CONNECTING;
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public ListenablePromise<String> acceptable(Method method) {
		ListenablePromise<String> lp = remoteApplication.acceptable(method);
		if (lp != null) {
			return lp;
		}
		UniqueObject uo = new UniqueObject();
		uo.setCall(true);
		HandlerDescriptor hd = new HandlerDescriptor();
		hd.setInterf(method.getDeclaringClass().getName());
		hd.setMethod(method.getName());
		Class<?>[] cls = method.getParameterTypes();
		String[] pts = new String[cls.length];
		for (int i = 0; i < cls.length; i++) {
			pts[i] = cls[i].getCanonicalName();
		}
		hd.setParameterTypes(pts);
		uo.setObj(hd);
		lp = super.send(uo, null);
		remoteApplication.addAcceptMethodPromise(lp);
		return lp;
	}

	public RemoteConfig getRemoteConfig() {
		return remoteConfig;
	}

	public void reset() {
		ListenablePromise<ServerTransport> lp = this.handshakePromise;
		if (lp != null && !lp.isDone()) {
			lp.cancel(true);
		}
		this.handshakePromise = new ListenablePromise<ServerTransport>();
	}

}
