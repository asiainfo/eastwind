package eastwind.io2;

public interface MessageListener<T> {

	public void onMessage(T message);
	
}
