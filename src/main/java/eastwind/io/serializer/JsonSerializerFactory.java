package eastwind.io.serializer;

public class JsonSerializerFactory implements SerializerFactory {

	@Override
	public Serializer newInstance() {
		return new JsonSerializer();
	}

	@Override
	public boolean isThreadSafe() {
		return true;
	}

	@Override
	public boolean isPrototype() {
		return false;
	}

}
