package eastwind.io;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * 重用小对象
 */
public class ObjectCache {

	private static Map<Object, Object> cache = Maps.newConcurrentMap();

	@SuppressWarnings("unchecked")
	public static <T> T getCache(T obj) {
		Object cached = cache.get(obj);
		if (cached == null) {
			cache.put(obj, obj);
			return obj;
		}
		return (T) cached;
	}
}
