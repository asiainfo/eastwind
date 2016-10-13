package eastwind.io.serializer;

public class SerializerFactoryHolder {

	private SerializerHolder internalSerializerHolder = new SerializerHolder(new InternalSerializerFactory());
	private SerializerHolder binarySerializerHolder;
	private SerializerHolder jsonSerializerHolder;

	public void setBinarySerializerFactory(SerializerFactory serializerFactory) {
		this.binarySerializerHolder = new SerializerHolder(serializerFactory);
	}

	public void setJsonSerializerFactory(SerializerFactory serializerFactory) {
		this.jsonSerializerHolder = new SerializerHolder(serializerFactory);
	}

	public Serializer getInternalSerializer() {
		return internalSerializerHolder.getSerializer();
	}
	
	public Serializer getBinarySerializer() {
		return binarySerializerHolder.getSerializer();
	}

	public Serializer getJsonSerializer() {
		return jsonSerializerHolder.getSerializer();
	}
}
