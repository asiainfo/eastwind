package boc.message.server;

import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;

/**
 * 大数据量map,分多次遍历
 */
public class MultipleMap<K, V> {
	private int shard;
	private ConcurrentMap<K, V>[] maps;

	public static <K, V> MultipleMap<K, V> newMultipleMap() {
		return newMultipleMap(4);
	}

	public static <K, V> MultipleMap<K, V> newMultipleMap(int power) {
		MultipleMap<K, V> multipleMap = new MultipleMap<K, V>();
		multipleMap.shard = 1 << power;
		multipleMap.shard--;
		multipleMap.maps = new ConcurrentMap[multipleMap.shard];
		for (int i = 0; i < multipleMap.shard; i++) {
			multipleMap.maps[i] = Maps.newConcurrentMap();
		}
		return multipleMap;
	}

	public int getShard() {
		return shard + 1;
	}

	public ConcurrentMap<K, V> getKeyMap(K k) {
		if (k == null) {
			return maps[0];
		}
		return maps[k.hashCode() & shard];
	}

	public ConcurrentMap<K, V> getShardMap(int shard) {
		return maps[shard];
	}
}
