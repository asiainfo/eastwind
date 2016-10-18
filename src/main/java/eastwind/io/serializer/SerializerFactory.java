package eastwind.io.serializer;

public interface SerializerFactory {
	
	Serializer newInstance();
	
	boolean isThreadSafe();
	
	boolean isPrototype();
}
