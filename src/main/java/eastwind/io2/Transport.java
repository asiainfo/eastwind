package eastwind.io2;

public interface Transport {

	boolean isActived();

	boolean isClosed();

	Throwable getTh();
	
	void attach(EndPoint endPoint);

	EndPoint endPoint();

	void push(NetworkTraffic networkTraffic);

	void addActiveListener(Listener<Transport> listener);

	void addCloseListener(Listener<Transport> listener);

	void addNetworkTrafficListener(Listener<NetworkTraffic> listener);
}
