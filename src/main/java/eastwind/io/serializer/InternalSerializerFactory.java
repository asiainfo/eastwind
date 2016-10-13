package eastwind.io.serializer;

public final class InternalSerializerFactory implements SerializerFactory {

	@Override
	public Serializer newInstance() {
		return new InternalSerializer();
	}

	@Override
	public boolean isThreadSafe() {
		return false;
	}

	@Override
	public boolean isPrototype() {
		return false;
	}

}
