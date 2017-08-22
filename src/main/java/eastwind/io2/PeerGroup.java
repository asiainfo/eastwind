package eastwind.io2;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class PeerGroup {

	private String group;
	private Set<SocketAddress> addresses;
	private Map<SocketAddress, NetworkPeer> addressNetworkPeers = new HashMap<SocketAddress, NetworkPeer>();
	private Map<String, NetworkPeer> uuidNetworkPeers = new HashMap<String, NetworkPeer>();
	
	public PeerGroup(String group) {
		this.group = group;
	}
	
	public NetworkPeer getNetworkPeer(String uuid) {
		return uuidNetworkPeers.get(uuid);
	}
	
	public NetworkPeer createNetworkPeer(String uuid) {
		return new DefaultNetworkPeer(uuid, group, null, null);
	}
	
	public Set<SocketAddress> getAddresses() {
		return addresses;
	}

	public void setAddresses(Set<SocketAddress> addresses) {
		this.addresses = addresses;
	}

	public String getGroup() {
		return group;
	}
}
