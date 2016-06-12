package eastwind.io3;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.ClassUtils;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eastwind.io.common.CommonUtils;
import eastwind.io.test.Hello;
import eastwind.io.test.HelloImpl;

public class ObjectHandlerRegistry implements Registrable {

	private static List<Class<?>> primitiveWrappers = Lists.newArrayList();
	private static List<Class<?>> primitives = Lists.newArrayList();

	static {
		primitiveWrappers.add(Byte.class);
		primitiveWrappers.add(Short.class);
		primitiveWrappers.add(Integer.class);
		primitiveWrappers.add(Long.class);
		primitiveWrappers.add(Float.class);
		primitiveWrappers.add(Double.class);

		primitives.add(byte.class);
		primitives.add(short.class);
		primitives.add(int.class);
		primitives.add(long.class);
		primitives.add(float.class);
		primitives.add(double.class);
	}

	private Map<Class<?>, CopyOnWriteArrayList<MessageListener<Object>>> messageListeners = Maps.newHashMap();

	private Map<String, List<RpcHandler>> rpcHandlers = Maps.newHashMap();

	private List<HanlderObj> hanlderObjs = Lists.newArrayList();

	public RpcHandler getHandler(String namespace) {
		return rpcHandlers.get(namespace).get(0);
	}

	public void registerHandler(Object obj) {
		List<Class<?>> interfs = Lists.newArrayList();
		addGenericInterfaces(interfs, obj.getClass());
		for (int i = 0; i < interfs.size(); i++) {
			addGenericInterfaces(interfs, interfs.get(i));
		}

		HanlderObj ho = new HanlderObj();
		ho.interfs = interfs;
		ho.alias = newAlias(obj);

		for (Method method : obj.getClass().getMethods()) {
			if (!Object.class.equals(method.getDeclaringClass())) {
				RpcHandler rpcHandler = new RpcHandler(obj, method, ho.alias);
				String key = ho.alias + "." + method.getName();
				List<RpcHandler> handlers = rpcHandlers.get(key);
				if (handlers == null) {
					handlers = Lists.newArrayList();
					rpcHandlers.put(key, handlers);
				}
				handlers.add(rpcHandler);
				ho.handlers.add(rpcHandler);
			}
		}
		
		hanlderObjs.add(ho);
	}

	public List<MessageListener<Object>> getListeners(Class<?> cls) {
		return messageListeners.get(cls);
	}

	public RpcHandler getHandler(String interf, String method, String[] parameterTypes)
			throws ClassNotFoundException {
		Class<?> cls = Class.forName(interf);
		Class<?>[] pts = new Class<?>[parameterTypes.length];
		for (int i = 0; i < pts.length; i++) {
			pts[i] = Class.forName(parameterTypes[i]);
		}

		RpcHandler handler = null;
		for (HanlderObj ho : hanlderObjs) {
			if (ho.interfs.contains(cls)) {
				int distance = 0;
				for (RpcHandler h : ho.handlers) {
					int d = distance(h.getTargetMethod(), pts);
					if (d == 0) {
						handler = h;
						break;
					}
					if (d != -1 && d < distance) {
						handler = h;
						distance = d;
					}
				}
				break;
			}
		}

		return handler;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void registerListener(MessageListener messageListener) {
		Class<?> cls = CommonUtils.getGenericType(messageListener.getClass(), MessageListener.class);
		CopyOnWriteArrayList<MessageListener<Object>> listeners = messageListeners.get(cls);
		if (listeners == null) {
			listeners = new CopyOnWriteArrayList<MessageListener<Object>>();
			messageListeners.put(cls, listeners);
		}
		listeners.add(messageListener);
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

	private String newAlias(Object obj) {
		String name = obj.getClass().getSimpleName();
		name = name.substring(0, 1).toLowerCase() + name.substring(1);
		if (!duplicateOfAlias(name)) {
			return name;
		}

		String fullName = obj.getClass().getCanonicalName();
		name = fullName.substring(0, fullName.lastIndexOf(".")) + "." + name;
		for (int i = name.length(); (i = name.lastIndexOf(".", i - 1)) != -1;) {
			String alias = name.substring(i + 1);
			if (duplicateOfAlias(alias)) {
				continue;
			}
			return alias;
		}
		return null;
	}

	private boolean duplicateOfAlias(String alias) {
		for (HanlderObj ho : hanlderObjs) {
			if (ho.alias.equals(alias)) {
				return true;
			}
		}
		return false;
	}

	private int distance(Method m, Class<?>[] parameterTypes) {
		Class<?>[] pts = m.getParameterTypes();
		if (pts.length != parameterTypes.length) {
			return -1;
		}
		int distance = 0;
		for (int i = 0; i < pts.length; i++) {
			Class<?> c1 = pts[i];
			Class<?> c2 = parameterTypes[i];
			if (c1 == c2) {
				continue;
			}
			if (c1.isAssignableFrom(c2)) {
				distance++;
				continue;
			}
			if (ClassUtils.isPrimitiveOrWrapper(c1) && ClassUtils.isPrimitiveOrWrapper(c2)) {
				if (ClassUtils.isPrimitiveWrapper(c1) && ClassUtils.isPrimitiveWrapper(c2)) {
					return -1;
				}
				if (c1 == (c2.isPrimitive() ? ClassUtils.primitiveToWrapper(c2) : ClassUtils.wrapperToPrimitive(c2))) {
					continue;
				}
				if (c1.isPrimitive()) {
					int i1 = primitives.indexOf(c1);
					int i2 = c2.isPrimitive() ? primitives.indexOf(c2) : primitiveWrappers.indexOf(c2);
					if (i1 > i2) {
						distance++;
						continue;
					}
				}
			}
			return -1;
		}
		return distance;
	}

	private static class HanlderObj {
		String alias;
		List<Class<?>> interfs;
		List<RpcHandler> handlers = Lists.newArrayList();
	}

	public static void main(String[] args) throws ClassNotFoundException {
		ObjectHandlerRegistry or = new ObjectHandlerRegistry();
		or.registerHandler(new HelloImpl());
		RpcHandler handler = or.getHandler(Hello.class.getCanonicalName(), "hello",
				new String[] { String.class.getCanonicalName() });
		System.out.println(JSON.toJSONString(handler));
	}
}
