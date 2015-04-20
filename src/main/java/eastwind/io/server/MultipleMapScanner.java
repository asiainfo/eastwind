package eastwind.io.server;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class MultipleMapScanner<K, V> implements Runnable {

	private int shard;
	protected int mills;
	protected MultipleMap<K, V> multipleMap;
	protected ScheduledExecutorService scheduledExecutorService;

	public MultipleMapScanner(int mills, MultipleMap<K, V> multipleMap,
			ScheduledExecutorService scheduledExecutorService) {
		this.mills = mills;
		this.multipleMap = multipleMap;
		this.scheduledExecutorService = scheduledExecutorService;
	}

	public void begin() {
		scheduledExecutorService.schedule(this, mills / multipleMap.getShard(), TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {
		long t1 = System.currentTimeMillis();
		ConcurrentMap<K, V> map = multipleMap.getShard(shard);
		prepare();
		for (V v : map.values()) {
			process(v);
		}
		long cost = System.currentTimeMillis() - t1;
		long nextRunTime = mills / multipleMap.getShard() - cost;
		shard++;
		if (shard >= multipleMap.getShard()) {
			shard %= multipleMap.getShard();
		}
		scheduledExecutorService.schedule(this, nextRunTime, TimeUnit.MILLISECONDS);
	}

	protected void prepare() {

	}

	protected abstract void process(V v);
}
