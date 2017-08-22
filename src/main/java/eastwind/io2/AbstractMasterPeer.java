package eastwind.io2;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import eastwind.io.ProviderHandler;
import eastwind.io.ProviderRegistry;
import eastwind.io.Sequencer;
import eastwind.io.support.MillisX10Sequencer;

public abstract class AbstractMasterPeer extends AbstractPeer implements MasterPeer {

	protected int masterThreads = 1;
	protected int workerThreads;
	protected InetSocketAddress localAddress = new InetSocketAddress(12468);
	protected AcceptableTransport acceptableTransport;
	protected NettyConnector nettyConnector;

	protected ProviderRegistry internalRegistry = new ProviderRegistry();
	protected ProviderRegistry providerRegistry = new ProviderRegistry();
	protected Map<String, PeerGroup> peerGroups = new HashMap<String, PeerGroup>();

	protected Sequencer sequencer = new MillisX10Sequencer();
	protected ConcurrentMap<Long, Exchange> exchanges = new ConcurrentHashMap<Long, Exchange>();

	private AcceptedListenerFactory acceptedListenerFactory = new AcceptedListenerFactory();
	private ConnectedListenerFactory connectedListenerFactory = new ConnectedListenerFactory();

	@Override
	public void start(InetSocketAddress localAddress) {
		if (localAddress == null) {
			this.localAddress = new InetSocketAddress(12469);
		} else {
			this.localAddress = localAddress;
		}
		nettyConnector = getConnectorFactory().createConnector(masterThreads, workerThreads);
		acceptableTransport = nettyConnector.accept(this.localAddress);
		acceptableTransport.addActiveListener(new AcceptorActiveListener());
	}

	protected abstract NettyConnectorFactory getConnectorFactory();

	@Override
	public void register(NetworkTrafficTransport transport) {
		NetworkListenerFactory lf = transport instanceof AcceptedTransport ? acceptedListenerFactory
				: connectedListenerFactory;
		transport.addPushListener(lf.getPushListener());
		transport.addShakeListener(lf.getShakeListener());
		transport.addActiveListener(lf.getActiveListener());
	}

	@Override
	public Exchange exchange(Request request) {
		Exchange exchange = new Exchange();
		exchange.setRequest(request);
		Long id = sequencer.get();
		request.setId(id);
		exchanges.put(id, exchange);
		return exchange;
	}

	@Override
	public ConnectedTransport connect(String group, SocketAddress remoteAddress) {
		ConnectedTransport transport = nettyConnector.connect(group, remoteAddress);
		register(transport);
		return transport;
	}

	@Override
	public PeerGroup getPeerGroup(String group) {
		PeerGroup pg = peerGroups.get(group);
		if (pg == null) {
			pg = new PeerGroup(group);
		}
		return pg;
	}

	@Override
	public NetworkPeer getNetworkPeer(String group, String uuid) {
		PeerGroup pg = getPeerGroup(group);
		NetworkPeer rp = pg.getNetworkPeer(uuid);
		if (rp == null) {
			return pg.createNetworkPeer(uuid);
		}
		return rp;
	}

	private class AcceptorActiveListener extends SyncListener<Transport> {
		@Override
		protected void doListen(Transport t) {
			if (acceptableTransport.isActived()) {
				logger.info("{} start on {}", group, localAddress);
			}
		}
	}

	private class ConnectedListenerFactory implements NetworkListenerFactory {

		ActiveListener activeListener = new ActiveListener();
		ShakeListener shakeListener = new ShakeListener();
		PushListener pushListener = new PushListener();

		@Override
		public Listener<Transport> getActiveListener() {
			return activeListener;
		}

		@Override
		public Listener<Shake> getShakeListener() {
			return shakeListener;
		}

		@Override
		public Listener<NetworkTraffic> getPushListener() {
			return pushListener;
		}

		private class ActiveListener implements Listener<Transport> {
			@Override
			public void listen(Transport t) {
				ConnectedTransport transport = (ConnectedTransport) t;
				if (transport.isActived()) {
					Shake shake = createShake();
					transport.post(shake);
				}
			}
		}

		private class ShakeListener implements Listener<Shake> {
			@Override
			public void listen(Shake t) {
				ConnectedTransport transport = (ConnectedTransport) t.getTransport();
				NetworkPeer rep = getNetworkPeer(t.getMyGroup(), t.getMyUuid());
				rep.attach(transport);
				transport.push(t);
			}
		}
	}

	private class AcceptedListenerFactory implements NetworkListenerFactory {

		Listener<Transport> activeListener = new EmptyListener();
		ShakeListener shakeListener = new ShakeListener();
		PushListener pushListener = new PushListener();

		@Override
		public Listener<Transport> getActiveListener() {
			return activeListener;
		}

		@Override
		public Listener<Shake> getShakeListener() {
			return shakeListener;
		}

		@Override
		public Listener<NetworkTraffic> getPushListener() {
			return pushListener;
		}

		private class EmptyListener implements Listener<Transport> {
			@Override
			public void listen(Transport t) {
			}
		}

		private class ShakeListener implements Listener<Shake> {

			@Override
			public void listen(Shake t) {
				Shake shake = createShake();
				NetworkPeer np = getNetworkPeer(t.getMyGroup(), t.getMyUuid());
				AcceptedTransport transport = (AcceptedTransport) t.getTransport();
				np.attach(transport);
				transport.post(shake);
			}

		}
	}

	private class PushListener implements Listener<NetworkTraffic> {

		@Override
		public void listen(NetworkTraffic t) {
			if (t instanceof Request) {
				try {
					onRequest((Request) t);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (t instanceof Response) {
				onResponse((Response) t);
			}
		}

		private void onRequest(Request request) throws Exception {
			ProviderRegistry registry = request.isInternal() ? internalRegistry : providerRegistry;
			ProviderHandler ph = registry.findHandler(request.getName());
			Object[] params = null;
			if (request.getDataLength() == 1) {
				params = new Object[] { request.getData() };
			} else if (request.getDataLength() > 1) {
				params = (Object[]) request.getData();
			}
			Object result = ph.invoke(params);
			if (result != Void.INSTANCE && !(result instanceof Void)) {
				Response response = new Response();
				response.setId(request.getId());
				response.setSerializer(request.getSerializer());
				response.setData(result);
				request.getTransport().post(response);
			}
		}

		private void onResponse(Response response) {
			Exchange exchange = exchanges.get(response.getId());
			if (exchange != null) {
				exchange.setResponse(response);
			}
		}
	}

}
