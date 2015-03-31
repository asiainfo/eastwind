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
		return newMultipleMap(5);
	}

	@SuppressWarnings("unchecked")
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

	public ConcurrentMap<K, V> getShard(int shard) {
		if (shard > this.shard) {
			shard &= this.shard;
		}
		return maps[shard];
	}

	public void clear() {
		for (ConcurrentMap<K, V> map : maps) {
			map.clear();
		}
	}

	public V get(K k) {
		return getShardByKey(k).get(k);
	}

	public V put(K k, V v) {
		return getShardByKey(k).put(k, v);
	}

	public V putIfAbsent(K k, V v) {
		return getShardByKey(k).putIfAbsent(k, v);
	}

	public V remove(K k) {
		return getShardByKey(k).remove(k);
	}

	public ConcurrentMap<K, V> getShardByKey(K k) {
		if (k == null) {
			return maps[0];
		}
		return maps[k.hashCode() & shard];
	}
}
