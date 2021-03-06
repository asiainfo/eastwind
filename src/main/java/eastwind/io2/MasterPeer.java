package eastwind.io2;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public interface MasterPeer extends Peer {

	void register(NetworkTrafficTransport transport);
	
	Exchange exchange(Request request);

	void start(InetSocketAddress localAddress);

	ConnectedTransport connect(String group, SocketAddress remoteAddress);

	PeerGroup getPeerGroup(String group);

	NetworkPeer getNetworkPeer(String group, String uuid);
	
}
