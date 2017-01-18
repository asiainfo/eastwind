package eastwind.io2;

import java.net.InetSocketAddress;

import com.google.common.eventbus.EventBus;

import eastwind.io.Provider;
import eastwind.io.ProviderHandler;

public abstract class AbstractAcceptorEndPoint extends AbstractConnectorEndPoint {

	protected InetSocketAddress localAddress = new InetSocketAddress(12468);
	protected AcceptorTransport acceptorTransport;

	protected EventBus acceptorEventBus;

	protected AbstractAcceptorEndPoint(String uuid, String group, String tag, String version) {
		super(uuid, group, tag, version);
	}

	protected void init() {
		super.init();
		acceptorEventBus = createEventBus(tag, localAddress);
		acceptorTransport = nettyConnector.accept(localAddress);
		acceptorTransport.addActiveListener(new AcceptorActiveListener());
	}

	protected void setLocalAddress(InetSocketAddress localAddress) {
		this.localAddress = localAddress;
	}

	private class AcceptorActiveListener extends SyncListener<Transport> {
		@Override
		protected void doListen(Transport t) {
			if (acceptorTransport.isActived()) {
				logger.info("{} start on {}", group, localAddress);
			}
		}
	}

	// after shake
	@Provider("")
	class AcceptorProvider {
		
		public ProviderDescriptor desc(ProviderDescriptor desc) {
			ProviderHandler ph = internalRegistry.findHandler(desc.getName());
			return ph == null ? null : ph.getDescriptor();
		}
	}

	@Override
	protected ListenerFactory getListenerFactory(NetworkTrafficTransport transport) {
		if (transport instanceof InboundTransport) {
			return new InboundListenerFactory((InboundTransport) transport);
		}
		return super.getListenerFactory(transport);
	}

	class InboundListenerFactory implements ListenerFactory {

		private InboundTransport transport;
		
		public InboundListenerFactory(InboundTransport transport) {
			this.transport = transport;
		}

		@Override
		public Listener<Transport> getActiveListener() {
			return null;
		}

		@Override
		public Listener<Shake> getShakeListener() {
			return new Listener<Shake>() {
				@Override
				public void listen(Shake t) {
					Shake shake = createShake();
					RemoteEndPoint rep = getOrCreateEndPoint(t.getMyGroup(), t.getMyUuid());
					rep.attach(transport);
					transport.post(shake);
				}
			};
		}

		@Override
		public Listener<NetworkTraffic> getNetworkTrafficListener() {
			return new Listener<NetworkTraffic>() {
				@Override
				public void listen(NetworkTraffic t) {
					acceptorEventBus.post(t);
				}
			};
		}
		
	}
}
