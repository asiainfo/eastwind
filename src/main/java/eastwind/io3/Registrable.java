package eastwind.io3;

public interface Registrable {

	public void registerHandler(Object instance);
	
	public <T> void registerListener(MessageListener<T> messageListener);
	
}
