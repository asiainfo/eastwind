package eastwind.io3;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

import eastwind.io.common.NamedThreadFactory;

public class ExpirationExecutor {

	private byte executorSize;

	private Executor[] executors;

	private NamedThreadFactory threadFactory = new NamedThreadFactory("ExpirationExecutor");

	public ExpirationExecutor(byte executorSize) {
		this.executorSize = executorSize;
		this.executors = new Executor[executorSize];
	}

	public <V> void add(ListenablePromise<V> promise) {
		int i = (byte) ((promise.getId() >> 1) & executorSize);
		Executor exe = executors[i];
		if (exe == null) {
			synchronized (executors) {
				if (exe == null) {
					exe = new Executor();
					exe.q = new DelayQueue<ListenablePromise<?>>();
					exe.thread = threadFactory.newThread(new ExpirationRunner(exe.q));
					exe.thread.start();
				}
			}
		}
		exe.q.add(promise);
	}

	public <V> void remove(ListenablePromise<V> promise) {
		if (promise.getDelay(TimeUnit.MILLISECONDS) > 100) {
			int i = (byte) ((promise.getId() >> 1) & executorSize);
			Executor exe = executors[i];
			exe.q.remove(promise);
		}
	}
	
	private static class Executor {
		Thread thread;
		DelayQueue<ListenablePromise<?>> q;
	}

	private static class ExpirationRunner implements Runnable {

		DelayQueue<ListenablePromise<?>> q;

		public ExpirationRunner(DelayQueue<ListenablePromise<?>> q) {
			this.q = q;
		}

		@Override
		public void run() {
			for (;;) {
				try {
					ListenablePromise<?> lp = q.take();
					lp.failed(null);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}
}
