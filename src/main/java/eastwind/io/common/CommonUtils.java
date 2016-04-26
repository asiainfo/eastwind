package eastwind.io.common;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentMap;

import eastwind.io3.MessageListener;

public class CommonUtils {

	public static String UUID;
	
	static {
		UUID = java.util.UUID.randomUUID().toString();
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
						return (Class<?>) ((ParameterizedType)tp).getRawType();
					}
				}
			}
		}
		return null;
	}
	
	public static long currentTimeSeconds() {
		return System.currentTimeMillis() / 1000;
	}

	public static <K, V> V putIfAbsent(ConcurrentMap<K, V> map, K key, V value) {
		V present = map.putIfAbsent(key, value);
		if (present == null) {
			return value;
		}
		return present;
	}
	
	public static Host toHost(String uri) {
		if (uri.contains(":")) {
			String[] arr = uri.split(":");
			return new Host(arr[0].trim(), Integer.parseInt(arr[1].trim()));
		}
		return new Host("127.0.0.1", Integer.parseInt(uri.trim()));
	}

	
}