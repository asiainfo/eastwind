package eastwind.io2;

import java.net.InetSocketAddress;

public interface Connector {

	Transport bind(InetSocketAddress localAddress);
	
	Transport connect(InetSocketAddress remoteAddress);
}