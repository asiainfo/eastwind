package eastwind.io3;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.collect.Maps;

public class ObjectHandlerRegistry {

	private Map<Class<?>, CopyOnWriteArrayList<MessageListener<Object>>> messageListeners = Maps.newHashMap();

	public List<MessageListener<Object>> getMessageListeners(Class<?> cls) {
		synchronized (messageListeners) {
			return messageListeners.get(cls);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void registerMessageListener(MessageListener messageListener) {
		Class<Object> cls = null;
		for (Type type : messageListener.getClass().getGenericInterfaces()) {
			if (type instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) type;
				if (pt.getRawType().equals(MessageListener.class)) {
					cls = (Class<Object>) pt.getActualTypeArguments()[0];
					break;
				}
			}
		}
		synchronized (messageListeners) {
			CopyOnWriteArrayList<MessageListener<Object>> listeners = messageListeners.get(cls);
			if (listeners == null) {
				listeners = new CopyOnWriteArrayList<MessageListener<Object>>();
				messageListeners.put(cls, listeners);
			}
			listeners.add(messageListener);
		}
	}


}
