package eastwind.io3;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import eastwind.io3.support.SettableFuture;


public class InvocationPromise<V> implements InvocationFuture<V> {

	private SettableFuture<V> future = new SettableFuture<V>();
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return future.cancel(mayInterruptIfRunning);
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isThrowable() {
		// TODO Auto-generated method stub
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
	
	public boolean set(V value) {
		return future.set(value);
	}

	public boolean setException(Throwable throwable) {
		return future.setException(throwable);
	}

}
