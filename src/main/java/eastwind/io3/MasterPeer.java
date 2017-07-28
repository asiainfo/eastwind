package eastwind.io3;

import java.net.InetSocketAddress;

import eastwind.io3.codex.HandlerInitializerSelector;
import eastwind.io3.connector.NettyConnector;

public class MasterPeer extends Peer {

	private Object context;
	private HandlerInitializerSelector handlerInitializerSelector;
	private AdjacencyPeerGroup adjacencyPeerGroup;
	private NettyConnector nettyConnector;
	private InetSocketAddress localAddress;
	
	public MasterPeer() {
		adjacencyPeerGroup = new AdjacencyPeerGroup();
		handlerInitializerSelector = new HandlerInitializerSelector();
		nettyConnector = new NettyConnector(handlerInitializerSelector);
	}
	
	public void start() {
		if (localAddress != null) {
			nettyConnector.setLocalAddress(localAddress);
		}
	}

	public Object getContext() {
		return context;
	}

	public void setContext(Object context) {
		this.context = context;
	}

	public InetSocketAddress getLocalAddress() {
		return localAddress;
	}

	public void setLocalAddress(InetSocketAddress localAddress) {
		this.localAddress = localAddress;
	}
}
