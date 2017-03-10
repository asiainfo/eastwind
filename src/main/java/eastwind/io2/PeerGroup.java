package eastwind.io2;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class PeerGroup {

	private String group;
	private Set<SocketAddress> addresses;
	private Map<SocketAddress, RelatedPeer> addressRelatedPeers = new HashMap<SocketAddress, RelatedPeer>();
	private Map<String, RelatedPeer> uuidRelatedPeers = new HashMap<String, RelatedPeer>();
	
	public PeerGroup(String group) {
		this.group = group;
	}
	
	public RelatedPeer getRelatedPeer(String uuid) {
		return uuidRelatedPeers.get(uuid);
	}
	
	public RelatedPeer createRelatedPeer(String uuid) {
		return new DefaultRelatedPeer(uuid, group, null, null);
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
