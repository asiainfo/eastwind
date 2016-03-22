package eastwind.io3;

public interface MessageListener<T> {

	public void onMessage(T message, Transport transport);
	
}