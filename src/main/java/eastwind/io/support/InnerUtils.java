package eastwind.io.support;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;

import eastwind.io.Provider;
import eastwind.io.model.Host;

public class InnerUtils {

	public static String getInstanceName(Class<?> cls) {
		Provider provider = cls.getAnnotation(Provider.class);
		if (provider != null && provider.value() != Provider.DEFAULT_NAME) {
			return provider.value().trim();
		}
		String simpleName = cls.getSimpleName();
		if (simpleName.length() == 1) {
			return simpleName.substring(0, 1).toLowerCase();
		}
		return simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
	}

	public static String getMethodName(Method method) {
		Provider provider = method.getAnnotation(Provider.class);
		if (provider == null || provider.value().equals(Provider.DEFAULT_NAME) || StringUtils.isBlank(provider.value())) {
			return method.getName();
		}
		return provider.value().trim();
	}

	public static String getFullProviderName(String namespace, String name) {
		if (StringUtils.isBlank(namespace)) {
			return name;
		}
		return namespace + "/" + name;
	}
	
	public static String[] getParameterTypes(Method method) {
		Class<?>[] cls = method.getParameterTypes();
		String[] pts = new String[cls.length];
		for (int i = 0; i < cls.length; i++) {
			pts[i] = cls[i].getCanonicalName();
		}
		return pts;
	}

	public static Class<?> getGenericType(Class<?> src, Class<?> interf) {
		for (Type type : src.getGenericInterfaces()) {
			if (type instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) type;
				if (pt.getRawType().equals(interf)) {
					Type tp = pt.getActualTypeArguments()[0];
					if (tp instanceof Class<?>) {
						return (Class<?>) tp;
					} else {
						return (Class<?>) ((ParameterizedType) tp).getRawType();
					}
				}
			}
		}
		return null;
	}

	public static <K, V> V putIfAbsent(ConcurrentMap<K, V> map, K key, V value) {
		V present = map.putIfAbsent(key, value);
		if (present == null) {
			return value;
		}
		return present;
	}

	public static <T> T getThreadLocal(ThreadLocal<T> threadLocal) {
		T t = threadLocal.get();
		threadLocal.set(null);
		return t;
	}
	
	public static Object returnNull(Method method) {
		Class<?> type = method.getReturnType();
		// boolean, char, byte, short, int, long, float, double
		if (type.isPrimitive()) {
			if (type == boolean.class) {
				return false;
			}

			if (type == int.class) {
				return Integer.MIN_VALUE;
			}
			if (type == long.class) {
				return Long.MIN_VALUE;
			}

			if (type == byte.class) {
				return Byte.MIN_VALUE;
			}
			if (type == short.class) {
				return Short.MIN_VALUE;
			}
			if (type == float.class) {
				return Float.MIN_VALUE;
			}
			if (type == double.class) {
				return Double.MIN_VALUE;
			}

			if (type == char.class) {
				return (char) 0xffff;
			}
		} else {
			return null;
		}
		return null;
	}
	
	public static Host toHost(String uri) {
		if (uri.contains(":")) {
			String[] arr = uri.split(":");
			return new Host(arr[0].trim(), Integer.parseInt(arr[1].trim()));
		}
		return new Host("127.0.0.1", Integer.parseInt(uri.trim()));
	}

}