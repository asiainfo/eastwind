package eastwind.io2;

import java.net.SocketAddress;
import java.util.Set;

public abstract class AbstractConfigurablePeer extends AbstractEventPeer implements ConfigurablePeer {
	
	public void refresh(String group, Set<SocketAddress> addresses) {
		PeerGroup pg = getPeerGroup(group);
		pg.setAddresses(addresses);
		for (SocketAddress address : addresses) {
			connect(group, address);
		}
	}
}
