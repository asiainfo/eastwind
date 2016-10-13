package eastwind.io.serializer;

public interface SerializerFactory {
	
	public Serializer newInstance();
	
	public boolean isThreadSafe();
	
	public boolean isPrototype();
}
