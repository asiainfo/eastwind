package eastwind.io3;

import com.google.common.util.concurrent.AbstractFuture;

public class ListenablePromise<V> extends AbstractFuture<V> implements Unique {

	protected long id;
	protected Unique message;
	protected Object attach;
	protected long time;
	protected long expiration;
	protected V v;

	public void succeeded() {
		super.set(null);
	}

	public void succeeded(V v) {
		super.set(v);
		this.v = v;
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

	public Unique getMessage() {
		return message;
	}

	public void setMessage(Unique message) {
		this.message = message;
	}

	public V getNow() {
		return v;
	}
	
	public void setId(long id) {
		this.id = id;
	}

	public void setTimeAndExpiration(long time, long expiration) {
		this.time = time;
		this.expiration = expiration;
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

}
