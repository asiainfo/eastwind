package eastwind.io2;

public interface NetworkTrafficTransport extends Transport {
	
	boolean post(NetworkTraffic networkTraffic);
	
	boolean isShaked();
	
	void addShakeListener(Listener<Transport> listener);
}
