package eastwind.io2;

import java.net.InetSocketAddress;
import java.util.UUID;

import eastwind.io.ProviderRegistry;

public abstract class AbstractLocalEndPoint extends AbstractEventEndPoint implements LocalEndPoint {

	public static int DEFAULT_WEIGHT = 10;
	
	protected Transport transport;
	protected InetSocketAddress localAddress= new InetSocketAddress("127.0.0.1", 12468);
	protected ProviderRegistry providerRegistry = new ProviderRegistry();
	protected ProviderRegistry internalRegistry = new ProviderRegistry();

	protected int masterThreads = 1;
	protected int workerThreads;
	protected NettyConnector nettyConnector;
	
	protected AbstractLocalEndPoint(String group, String tag, String version, int weight) {
		super(UUID.randomUUID().toString(), group, tag, version, weight);
	}
	
	protected abstract NettyConnectorFactory getConnectorFactory();
	
	protected void start() {
		nettyConnector = getConnectorFactory().createConnector(masterThreads, workerThreads);
		Transport transport = nettyConnector.bind(localAddress);
		transport.attach(this);
		ActiveListener listener = new ActiveListener();
		transport.addActiveListener(listener);
		listener.sync();
	}
	
	class ActiveListener extends SyncListener<Transport> {
		@Override
		protected void doListen(Transport t) {
			if (t.isActived()) {
				logger.info("{} start on {}", group, localAddress);
			}
		}
	}
	
	public void setMasterThreads(int masterThreads) {
		this.masterThreads = masterThreads;
	}

	public void setWorkerThreads(int workerThreads) {
		this.workerThreads = workerThreads;
	}

	@Override
	public void attach(Transport transport) {
		this.transport = transport;
		transport.attach(this);
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		return localAddress;
	}

	@Override
	public void registerProvider(Object provider) {
		providerRegistry.registerProvider(provider);
	}

	@Override
	public void setLocalAddress(InetSocketAddress localAddress) {
		this.localAddress = localAddress;
	}

	protected void registerInternalProvider(Object provider) {
		internalRegistry.registerProvider(provider);
	}

}
