package eastwind.io3.serializer;

public interface SerializerFactory {
	
	public Serializer newInstance();
	
	public boolean isThreadSafe();
	
	public boolean isPrototype();
}
