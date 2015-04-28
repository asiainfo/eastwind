package eastwind.io.server;

import java.util.Iterator;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;

/**
 * 大数据量map,分多次遍历
 */
public class MultipleMap<K, V> implements Iterable<V> {
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
		multipleMap.maps = new ConcurrentMap[multipleMap.shard + 1];
		for (int i = 0; i < multipleMap.maps.length; i++) {
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

	@Override
	public Iterator<V> iterator() {
		return new It();
	}

	public static void main(String[] args) {
		MultipleMap<String, String> map = MultipleMap.newMultipleMap();
		for (int i = 0; i < 100; i++) {
			map.put(i + "", i + "");
		}
		Iterator<String> it = map.iterator();
		for (int i = 0; it.hasNext(); i++) {
			System.out.println(i + ":" + it.next());
		}
	}

	private class It implements Iterator<V> {

		private int curShard = 0;
		private Iterator<V> curIt = maps[0].values().iterator();

		@Override
		public boolean hasNext() {
			while (curShard <= shard) {
				if (curIt.hasNext()) {
					return true;
				} else {
					curShard++;
					if (curShard <= shard) {
						curIt = maps[curShard].values().iterator();
					} else {
						return false;
					}
				}
			}
			return false;
		}

		@Override
		public V next() {
			while (curShard <= shard) {
				if (curIt.hasNext()) {
					return curIt.next();
				} else {
					curShard++;
					if (curShard <= shard) {
						curIt = maps[curShard].values().iterator();
					}
				}
			}
			return null;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}
}
