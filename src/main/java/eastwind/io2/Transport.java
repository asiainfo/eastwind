package eastwind.io2;

public interface Transport {

	boolean isActived();

	boolean isClosed();

	Throwable getTh();
	
	void attach(EndPoint endPoint);

	EndPoint endPoint();

	void addActiveListener(Listener<Transport> listener);

	void addCloseListener(Listener<Transport> listener);

}
