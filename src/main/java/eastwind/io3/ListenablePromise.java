package eastwind.io3;

import com.google.common.util.concurrent.AbstractFuture;

public class ListenablePromise<V> extends AbstractFuture<V> implements Unique {

	protected long id;
	protected Unique message;
	protected Object attach;
	protected long time = System.currentTimeMillis();
	protected V v;
	protected Throwable th;
	
	public void succeeded() {
		super.set(null);
	}

	public void succeeded(V v) {
		super.set(v);
		this.v = v;
	}

	public void failed(Throwable th) {
		this.th = th;
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

	public Throwable getTh() {
		return th;
	}

	public V getNow() {
		return v;
	}
	
	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public long getTime() {
		return time;
	}

	public void addListener(final OperationListener<ListenablePromise<V>> listener) {
		super.addListener(new Runnable() {
			@Override
			public void run() {
				listener.complete(ListenablePromise.this);
			}
		}, GlobalExecutor.EVENT_EXECUTOR);
	}

}