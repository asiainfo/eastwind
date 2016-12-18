package eastwind.io2;

import java.net.InetSocketAddress;

public interface NetworkTrafficEndPoint extends EndPoint {

	InetSocketAddress getRemoteAddress();

	boolean invokable();
	
	void send(Object message);
	
	Exchange send(Request request);
	
}
