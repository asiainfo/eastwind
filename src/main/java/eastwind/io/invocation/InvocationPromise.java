package eastwind.io.invocation;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import eastwind.io.support.GlobalExecutor;
import eastwind.io.support.SettableFuture;

public class InvocationPromise<V> implements InvocationFuture<V> {

	@SuppressWarnings("rawtypes")
	public static final ThreadLocal<InvocationPromise> TL = new ThreadLocal<InvocationPromise>();
	
	private SettableFuture<V> future = new SettableFuture<V>();
	private byte state;

	public boolean set(V value) {
		boolean r = future.set(value);
		if (r) {
			this.state = 1;
		}
		return r;
	}

	public boolean setException(Throwable throwable) {
		boolean r = future.setException(throwable);
		if (r) {
			this.state = 2;
		}
		return r;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		boolean r = future.cancel(mayInterruptIfRunning);
		if (r) {
			this.state = 3;
		}
		return r;
	}

	@Override
	public boolean isDone() {
		return future.isDone();
	}

	@Override
	public boolean isCancelled() {
		return future.isCancelled();
	}

	@Override
	public boolean isSuccess() {
		return false;
	}

	@Override
	public boolean isThrowable() {
		return false;
	}

	@Override
	public V get() throws InterruptedException, ExecutionException {
		return future.get();
	}

	@Override
	public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return future.get(timeout, unit);
	}

	@Override
	public void addListener(final InvocationListener<V> listener) {
		future.addListener(new Runnable() {
			@Override
			public void run() {
				if (state == 1) {
					listener.onResult(InvocationPromise.this);
				} else if (state == 2) {
					listener.onExecutionException(InvocationPromise.this);
				} else if (state == 0 || state == 3) {
					listener.onCanceled(InvocationPromise.this);
				}
			}
		}, GlobalExecutor.EVENT_EXECUTOR);
	}
}
