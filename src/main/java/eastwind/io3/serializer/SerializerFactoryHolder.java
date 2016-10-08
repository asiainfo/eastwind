package eastwind.io3.serializer;

public class SerializerFactoryHolder {

	private SerializerHolder binarySerializerHolder;
	private SerializerHolder jsonSerializerHolder;
	
	public void setBinarySerializerFactory(SerializerFactory serializerFactory) {
		this.binarySerializerHolder = new SerializerHolder(serializerFactory);
	}
	
	public void setJsonSerializerFactory(SerializerFactory serializerFactory) {
		this.jsonSerializerHolder = new SerializerHolder(serializerFactory);
	}
	
	public Serializer getBinarySerializer() {
		return binarySerializerHolder.getSerializer();
	}
	
	public Serializer getJsonSerializer() {
		return jsonSerializerHolder.getSerializer();
	}
}
