package eastwind.io.serializer;

public class SerializerFactoryHolder {

	private SerializerHolder frameworkSerializerHolder = new SerializerHolder(new KryoSerializerFactory());
	private SerializerHolder proxySerializerHolder;
	private SerializerHolder smartSerializerHolder;

	public void setProxySerializerFactory(SerializerFactory serializerFactory) {
		this.proxySerializerHolder = new SerializerHolder(serializerFactory);
	}

	public void setSmartSerializerFactory(SerializerFactory serializerFactory) {
		this.smartSerializerHolder = new SerializerHolder(serializerFactory);
	}

	public Serializer getFrameworkSerializer() {
		return frameworkSerializerHolder.getSerializer();
	}

	public Serializer getProxySerializer() {
		if (proxySerializerHolder == null) {
			proxySerializerHolder = new SerializerHolder(new KryoSerializerFactory());
		}
		return proxySerializerHolder.getSerializer();
	}

	public Serializer getSmartSerializer() {
		if (smartSerializerHolder == null) {
			smartSerializerHolder = new SerializerHolder(new JsonSerializerFactory());
		}
		return smartSerializerHolder.getSerializer();
	}
}
