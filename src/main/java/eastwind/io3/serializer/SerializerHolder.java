package eastwind.io3.serializer;

import java.util.Map;

import com.google.common.collect.Maps;

public class SerializerHolder {

	private static ThreadLocal<Map<SerializerFactory, Serializer>> TL = new ThreadLocal<Map<SerializerFactory, Serializer>>() {
		@Override
		protected Map<SerializerFactory, Serializer> initialValue() {
			return Maps.newHashMap();
		}
	};
	private Serializer serializer;
	private SerializerFactory serializerFactory;

	public SerializerHolder(SerializerFactory serializerFactory) {
		this.serializerFactory = serializerFactory;
	}

	public Serializer getSerializer() {
		if (serializerFactory.isPrototype()) {
			return serializerFactory.newInstance();
		}
		if (serializerFactory.isThreadSafe()) {
			synchronized (this) {
				if (serializer == null) {
					serializer = serializerFactory.newInstance();
				}
				return serializer;
			}
		} else {
			Map<SerializerFactory, Serializer> serializers = TL.get();
			Serializer serializer = serializers.get(serializerFactory);
			if (serializer != null) {
				return serializer;
			}
			serializer = serializerFactory.newInstance();
			serializers.put(serializerFactory, serializer);
			return serializer;
		}
	}
}
