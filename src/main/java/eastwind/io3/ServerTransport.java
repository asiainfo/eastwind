package eastwind.io3;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;

import java.lang.reflect.Method;

public class ServerTransport extends Transport {

	private ServerConfig serverConfig;
	private ApplicationManager applicationManager;

	public ServerTransport(ServerConfig serverConfig, TransportSustainer transportSustainer,
			ApplicationManager applicationManager) {
		super(serverConfig.getGroup(), transportSustainer);
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
	protected void publish0(final TransportPromise tp) {
		Channel channel = getChannel();
		if (isReady()) {
			channel.writeAndFlush(tp.getMessage()).addListener(new FlushListener(tp));
		}
		
		if (channel == null || !channel.isActive() || state == CONNECT_FAIL) {
			retry(tp);
			return;
		}
	}

	@SuppressWarnings("rawtypes")
	private void retry(final TransportPromise tp) {
		applicationManager.connectNow(this);
		tp.setRetry(true);
		addHandshakeListener(new OperationListener<ServerTransport>() {
			@Override
			public void operationComplete(ServerTransport t) {
				Channel channel = getChannel();
				if (t.isReady()) {
					tp.setRetry(false);
					channel.writeAndFlush(tp.getMessage()).addListener(new FlushListener(tp));
				} else {
					// TODO fail
				}
			}
		});
	}

	private class FlushListener implements GenericFutureListener<ChannelFuture> {
	
		@SuppressWarnings("rawtypes")
		private TransportPromise tp;
		
		@SuppressWarnings({ "rawtypes" })
		public FlushListener(TransportPromise tp) {
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
		lp = super.publish(uo, null);
		remoteApplication.addAcceptMethodPromise(lp);
		return lp;
	}

	public ServerConfig getServerConfig() {
		return serverConfig;
	}

	public void reset() {
		this.handshakePromise = new ListenablePromise<ServerTransport>();
	}

}
