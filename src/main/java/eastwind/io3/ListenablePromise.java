package eastwind.io3;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.AbstractFuture;

public class ListenablePromise<V> extends AbstractFuture<V> implements Delayed {

	protected long id;
	protected Object attach;
	protected long time;
	protected long expiration;

	protected long exeTime;

	public void succeeded() {
		super.set(null);
	}

	public void succeeded(V v) {
		super.set(v);
	}

	public void failed(Throwable th) {
		super.setException(th);
	}

	public void setAttach(Object attach) {
		this.attach = attach;
	}

	public Object getAttach() {
		return attach;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setTimeAndExpiration(long time, long expiration) {
		this.time = time;
		this.expiration = expiration;
		this.exeTime = this.time + this.expiration;
	}

	public long getId() {
		return id;
	}

	public long getTime() {
		return time;
	}

	public long getExpiration() {
		return expiration;
	}

	public void addListener(final OperationListener<ListenablePromise<V>> listener) {
		super.addListener(new Runnable() {
			@Override
			public void run() {
				listener.operationComplete(ListenablePromise.this);
			}
		}, GlobalExecutor.EVENT_EXECUTOR);
	}

	@Override
	public int compareTo(Delayed o) {
		if (o instanceof ListenablePromise) {
			@SuppressWarnings("rawtypes")
			ListenablePromise lp2 = (ListenablePromise) o;
			if (exeTime < lp2.exeTime) {
				return -1;
			} else if (exeTime > lp2.exeTime) {
				return 1;
			} else {
				return 0;
			}
		} else {
			long d1 = getDelay(TimeUnit.MILLISECONDS);
			long d2 = o.getDelay(TimeUnit.MILLISECONDS);
			if (d1 < d2) {
				return -1;
			} else if (d1 > d2) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(exeTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}

}
