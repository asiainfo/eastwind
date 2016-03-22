package eastwind.io3;

public interface Registrable {

	public void registerRpcHandler(Object instance);
	
	public <T> void registerMessageListener(MessageListener<T> messageListener);
	
}
