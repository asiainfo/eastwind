package eastwind.io2;

public interface EventEndPoint extends EndPoint {

	void listenTo(NetworkTrafficTransport transport);
	
}
