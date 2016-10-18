package eastwind.io.serializer;

public class SerializerFactoryHolder {

	private SerializerHolder frameworkSerializerHolder = new SerializerHolder(new KryoSerializerFactory());
	private SerializerHolder binarySerializerHolder;
	private SerializerHolder jsonSerializerHolder;

	public void setBinarySerializerFactory(SerializerFactory serializerFactory) {
		this.binarySerializerHolder = new SerializerHolder(serializerFactory);
	}

	public void setJsonSerializerFactory(SerializerFactory serializerFactory) {
		this.jsonSerializerHolder = new SerializerHolder(serializerFactory);
	}

	public Serializer getFrameworkSerializer() {
		return frameworkSerializerHolder.getSerializer();
	}

	public Serializer getBinarySerializer() {
		return binarySerializerHolder == null ? null : binarySerializerHolder.getSerializer();
	}

	public Serializer getJsonSerializer() {
		return jsonSerializerHolder == null ? null : jsonSerializerHolder.getSerializer();
	}
}
