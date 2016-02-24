package eastwind.io2;

public interface MessageListener<T> {

	public Object onMessage(T message);
	
}
