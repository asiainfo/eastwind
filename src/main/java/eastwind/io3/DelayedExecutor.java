package eastwind.io3;

import java.util.Map;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Maps;

import eastwind.io.common.CommonUtils;

public class DelayedExecutor {

	private Thread t;
	private Sequence sequence = new MillisX10Sequence();
	private volatile DelayQueue<DelayedTask> current;
	private DelayQueue<DelayedTask>[] queues;
	private int cardinal;
	private Map<Class<?>, DelayedListener<?>> operations = Maps.newHashMap();
	
	@SuppressWarnings("unchecked")
	public DelayedExecutor() {
		queues = new DelayQueue[16];
		for (int i = 0; i < queues.length; i++) {
			queues[i] = new DelayQueue<DelayedTask>();
		}
		cardinal = queues.length - 1;
		
		t = new Thread(new Runnable() {
			@Override
			public void run() {
				for (;;) {
					DelayedTask pressing = null;
					for (DelayQueue<DelayedTask> q : queues) {
						DelayedTask dt = q.peek();
						if (dt != null && (pressing == null || dt.compareTo(pressing) < 0)) {
							pressing = dt;
							current = q;
						}
					}
					if (Thread.interrupted()) {
						continue;
					}
					try {
						if (pressing == null) {
							TimeUnit.SECONDS.sleep(2);
						} else {
							DelayedTask dt = current.poll(1, TimeUnit.SECONDS);
							if (dt != null) {
								@SuppressWarnings("rawtypes")
								DelayedListener ol = operations.get(dt.obj.getClass());
								ol.timeUp(dt.obj, DelayedExecutor.this);
							}
						}
					} catch (InterruptedException e) {
						if (Thread.interrupted()) {
							continue;
						}
					}
				}
			}
		});
		t.start();
	}

	public void register(DelayedListener<?> delayedListener) {
		Class<?> cls = CommonUtils.getGenericType(delayedListener.getClass(), DelayedListener.class);
		operations.put(cls, delayedListener);
	}
	
	public DelayedTask submit(Object task, long millis) {
		DelayedTask dt = new DelayedTask(sequence.get(), task, millis);
		submit0(dt);
		return dt;
	}

	public void resubmit(DelayedTask dt, long millis) {
		dt.setExeTime(dt, millis);
		submit0(dt);
	}
	
	private void submit0(DelayedTask dt) {
		int i = (byte) ((dt.id >> 1) & cardinal);
		DelayQueue<DelayedTask> q = queues[i];
		DelayedTask oldHead = q.peek();
		q.add(dt);
		checkCurrentQueue(oldHead, q);
	}

	public void cancel(long id) {
		int i = (byte) ((id >> 1) & cardinal);
		DelayQueue<DelayedTask> q = queues[i];
		DelayedTask oldHead = q.peek();
		q.remove(id);
		checkCurrentQueue(oldHead, q);
	}
	
	private void checkCurrentQueue(DelayedTask oldHead, DelayQueue<DelayedTask> q) {
		if (q == current) {
			return;
		}
		DelayedTask dt = null;
		if (current == null || (dt = current.peek()) == null) {
			t.interrupt();
			return;
		}
		DelayedTask qdt = q.peek();
		if (oldHead == qdt) {
			return;
		}
		if (qdt.compareTo(dt) == -1) {
			t.interrupt();
		}
	}

}
