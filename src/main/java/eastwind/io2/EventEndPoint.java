package eastwind.io2;

import java.net.InetSocketAddress;

public interface EventEndPoint extends EndPoint {

	void register(Object listener);
	
	void post(Object event);
	
	InetSocketAddress getLocalAddress();
}