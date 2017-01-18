package eastwind.io2;

import java.net.InetSocketAddress;

public interface NetworkTrafficTransport extends Transport {
	
	InetSocketAddress getRemoteAddress();
	
	void push(NetworkTraffic networkTraffic);
	
	boolean post(NetworkTraffic networkTraffic);
	
	boolean isShaked();
	
	Shake getShake();
	
	void addShakeListener(Listener<Shake> listener);
	
	void addNetworkTrafficListener(Listener<NetworkTraffic> listener);
}
