package eastwind.io2;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;

public interface NetworkPeer extends Peer {

	InetSocketAddress getRemoteAddress();

	void attach(Transport transport);

	ConnectedTransport getConnectedTransport();

	void addProviderSign(ProviderSign desc);

	ProviderSign resolveSign(Method method);

	ProviderSign resolveSign(String name);
}
