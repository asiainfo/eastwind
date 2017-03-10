package eastwind.io.support;

import java.util.Map;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Maps;

import eastwind.io.Sequencer;

public class DelayedExecutor {

	private Thread t;
	private Sequencer sequence = new MillisX10Sequencer();
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
								DelayedListener ol = operations.get(dt.cls);
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
		operations.put(delayedListener.getClass(), delayedListener);
	}

	public DelayedTask submit(Class<?> cls, Object obj, long millis) {
		DelayedTask dt = new DelayedTask(sequence.get(), cls, obj, millis);
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

	public void cancel(DelayedTask dt) {
		int i = (byte) ((dt.id >> 1) & cardinal);
		DelayQueue<DelayedTask> q = queues[i];
		DelayedTask oldHead = q.peek();
		q.remove(dt);
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
