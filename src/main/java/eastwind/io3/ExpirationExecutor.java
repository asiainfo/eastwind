package eastwind.io3;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class ExpirationExecutor {

	private Thread t;

	private volatile DelayQueue<ListenablePromise<?>> current;
	private DelayQueue<ListenablePromise<?>>[] queues;

	public static void main(String[] args) throws IOException {
		ExpirationExecutor expirationExecutor = new ExpirationExecutor();
		for (int i = 0; i < 100; i++) {
			expirationExecutor.add(lp());
		}
		System.in.read();
	}

	static Random random = new Random(System.currentTimeMillis());
	static AtomicInteger i = new AtomicInteger();

	private static ListenablePromise<?> lp() {
		ListenablePromise<Void> t = new ListenablePromise<Void>();
		t.setAttach(i.incrementAndGet());
		t.setTimeAndExpiration(System.currentTimeMillis(), random.nextInt(10000));
		t.addListener(new OperationListener<ListenablePromise<Void>>() {
			@Override
			public void operationComplete(ListenablePromise<Void> t) {
				System.out.println(t.getAttach() + ":" + System.currentTimeMillis() + "--" + t.exeTime + "--"
						+ t.expiration);
			}
		});
		return t;
	}

	@SuppressWarnings("unchecked")
	public ExpirationExecutor() {
		queues = new DelayQueue[8];
		for (int i = 0; i < queues.length; i++) {
			queues[i] = new DelayQueue<ListenablePromise<?>>();
		}

		t = new Thread(new Runnable() {
			@Override
			public void run() {
				for (;;) {
					ListenablePromise<?> pressing = null;
					for (DelayQueue<ListenablePromise<?>> q : queues) {
						ListenablePromise<?> lp = q.peek();
						if (lp != null && (pressing == null || lp.compareTo(pressing) < 0)) {
							pressing = lp;
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
							ListenablePromise<?> lp = current.poll(2, TimeUnit.SECONDS);
							if (lp != null && !lp.isDone()) {
								lp.failed(new TimeoutException());
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

	public void add(ListenablePromise<?> promise) {
		int i = (byte) ((promise.getId() >> 1) & queues.length);
		DelayQueue<ListenablePromise<?>> q = queues[i];
		ListenablePromise<?> oldHead = q.peek();
		q.add(promise);
		checkCurrentQueue(oldHead, q);
	}

	public void remove(ListenablePromise<?> promise) {
		int i = (byte) ((promise.getId() >> 1) & queues.length);
		DelayQueue<ListenablePromise<?>> q = queues[i];
		ListenablePromise<?> oldHead = q.peek();
		q.remove(promise);
		checkCurrentQueue(oldHead, q);
	}

	private void checkCurrentQueue(ListenablePromise<?> oldHead, DelayQueue<ListenablePromise<?>> q) {
		if (q == current) {
			return;
		}
		ListenablePromise<?> clp = null;
		if (current == null || (clp = current.peek()) == null) {
			t.interrupt();
			return;
		}
		ListenablePromise<?> qlp = q.peek();
		if (oldHead == qlp) {
			return;
		}
		if (qlp.compareTo(clp) == -1) {
			t.interrupt();
		}
	}
}
