package boc.message.common;

import java.util.concurrent.ConcurrentMap;

public class CommonUtils {

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