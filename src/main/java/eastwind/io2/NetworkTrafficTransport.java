package eastwind.io2;

import java.net.InetSocketAddress;

public interface NetworkTrafficTransport extends Transport {
	
	InetSocketAddress getRemoteAddress();
	
	void push(NetworkTraffic networkTraffic);
	
	void post(NetworkTraffic networkTraffic);
	
	public Exchange exchange(Request request);
	
	boolean isShaked();
	
	Shake getShake();
	
	void addShakeListener(Listener<Shake> listener);
	
	void addPushListener(Listener<NetworkTraffic> listener);
}
