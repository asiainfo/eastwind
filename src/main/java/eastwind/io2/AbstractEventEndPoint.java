package eastwind.io2;

import java.net.InetSocketAddress;

import com.google.common.eventbus.EventBus;

public abstract class AbstractEventEndPoint extends AbstractEndPoint implements EventEndPoint {

	protected int masterThreads = 1;
	protected int workerThreads;
	protected NettyConnector nettyConnector;
	
	protected AbstractEventEndPoint(String uuid, String group, String tag, String version) {
		super(uuid, group, tag, version);
	}

	protected void init() {
		nettyConnector = getConnectorFactory().createConnector(masterThreads, workerThreads);
	}
	
	protected abstract NettyConnectorFactory getConnectorFactory();
	
	protected EventBus createEventBus(String tag, InetSocketAddress address) {
		StringBuilder sb = new StringBuilder();
		sb.append(tag).append("-");
		sb.append(group).append("-");
		sb.append(uuid);
		return new EventBus(sb.toString());
	}
	
	protected abstract ListenerFactory getListenerFactory(NetworkTrafficTransport transport);
	
	public void listenTo(NetworkTrafficTransport transport) {
		ListenerFactory listenerFactory = getListenerFactory(transport);
		transport.addNetworkTrafficListener(listenerFactory.getNetworkTrafficListener());
		transport.addShakeListener(listenerFactory.getShakeListener());
		transport.addActiveListener(listenerFactory.getActiveListener());
	}
}
