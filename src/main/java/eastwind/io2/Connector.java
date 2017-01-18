package eastwind.io2;

import java.net.InetSocketAddress;

public interface Connector {

	AcceptorTransport accept(InetSocketAddress localAddress);
	
	OutboundTransport connect(String group, InetSocketAddress remoteAddress);
}