package eastwind.io2;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eastwind.io3.RpcHandler;

public class ObjectHandlerRegistry {

	private Map<Class<?>, CopyOnWriteArrayList<MessageListener<Object>>> messageListeners = Maps.newHashMap();

	private Map<String, List<RpcHandler>> rpcHandlers = Maps.newHashMap();

	public RpcHandler getRpcHandler(String namespace, int paramLen) {
		List<RpcHandler> handlers = rpcHandlers.get(namespace);
		if (handlers == null) {
			return null;
		}
		for (RpcHandler handler : handlers) {
			if (handler.getParamLen() == paramLen) {
				return handler;
			}
		}
		return null;
	}
	
	public void registerRpcHandler(Object instance) {
		List<Class<?>> interfs = Lists.newArrayList();
		addGenericInterfaces(interfs, instance.getClass());
		for (int i = 0; i < interfs.size(); i++) {
			addGenericInterfaces(interfs, interfs.get(i));
		}

		Set<Method> methods = Sets.newHashSet();
		for (int i = interfs.size() - 1; i >= 0; i--) {
			Class<?> c = interfs.get(i);
			for (Method m : c.getMethods()) {
				methods.add(m);
			}
		}

		for (Method method : methods) {
			synchronized (rpcHandlers) {
				RpcHandler rpcHandler = new RpcHandler(instance, method, "");
				String key = EastwindUtils.getDefaultNamespace(method);
				List<RpcHandler> handlers = rpcHandlers.get(key);
				if (handlers == null) {
					handlers = Lists.newArrayList();
					rpcHandlers.put(key, handlers);
				}
				handlers.add(rpcHandler);
			}
		}
	}

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

	private void addGenericInterfaces(List<Class<?>> interfs, Class<?> c) {
		for (Type t : c.getGenericInterfaces()) {
			if (Class.class.isInstance(t)) {
				interfs.add((Class<?>) t);
			} else if (ParameterizedType.class.isInstance(t)) {
				Type tp = ((ParameterizedType) t).getRawType();
				if (Class.class.isInstance(tp)) {
					interfs.add((Class<?>) tp);
				}
			}
		}
	}

}
