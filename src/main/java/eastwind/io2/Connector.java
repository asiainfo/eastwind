package eastwind.io2;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public interface Connector {

	AcceptableTransport accept(InetSocketAddress localAddress);
	
	ConnectedTransport connect(String group, SocketAddress remoteAddress);
}