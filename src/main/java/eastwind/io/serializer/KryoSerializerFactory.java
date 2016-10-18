package eastwind.io.serializer;

public final class KryoSerializerFactory implements SerializerFactory {

	@Override
	public Serializer newInstance() {
		return new KryoSerializer();
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
