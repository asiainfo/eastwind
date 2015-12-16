package eastwind.io2.client;

import java.util.LinkedList;
import java.util.List;

import eastwind.io.common.Host;
import eastwind.io2.Request;
import eastwind.io2.Response;

public class RpcContext<R> {

	public static ThreadLocal<RpcContext<?>> LOCAL = new ThreadLocal<RpcContext<?>>();
	
	private boolean sync = true;
	private Host host;

	private Request request;
	private Response response;

	private LinkedList<ResultListener<R>> listeners;

	// 1:ok -1:connet fail -2:timeout
	private int[] stat = new int[1];

	public void addListener(ResultListener<R> listener) {
		boolean added = false;
		synchronized (this) {
			if (stat[0] == 0) {
				this.getListeners().add(listener);
				added = true;
			}
		}
		if (!added) {
			listener.operationComplete(getResult(), getTh());
		}
	}

	public R sync() {
		synchronized (stat) {
			this.sync = true;
			if (stat[0] == 0) {
				getListeners().add(new ResultListener<R>() {
					@Override
					public void operationComplete(R result, Throwable th) {
						synchronized (stat) {
							stat.notifyAll();
						}
					}
				});

				try {
					stat.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		if (stat[0] == 1) {
			return getResult();
		} else if (stat[0] == -2) {
			throw new RuntimeException("timeout");
		} else {
			throw new RuntimeException("host is invalid");
		}
	}

	public void done(Response response) {
		synchronized (this) {
			this.stat[0] = 1;
			this.response = response;
		}
		invokeListener();
	}

	public void fail() {
		synchronized (this) {
			this.stat[0] = -1;
		}
		invokeListener();
	}

	public void timeout() {
		synchronized (this) {
			this.stat[0] = -2;
		}
		invokeListener();
	}
	
	public long getId() {
		return request.getHeader().getId();
	}
	
	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}
	
	public Host getHost() {
		return host;
	}

	public void setHost(Host host) {
		this.host = host;
	}

	public void setSync(boolean sync) {
		this.sync = sync;
	}

	public boolean isSync() {
		return sync;
	}

	private void invokeListener() {
		if (this.listeners == null) {
			return;
		}
		R r = getResult();
		Throwable th = getTh();
		for (int i = 0; i < getListeners().size(); i++) {
			getListeners().get(i).operationComplete(r, th);
		}
	}

	private Throwable getTh() {
		return response.getHeader().isTh() ? (Throwable) response.getResult() : null;
	}

	@SuppressWarnings("unchecked")
	private R getResult() {
		return (R) (response.getHeader().isTh() ? null : response.getResult());
	}

	private synchronized List<ResultListener<R>> getListeners() {
		if (listeners == null) {
			listeners = new LinkedList<ResultListener<R>>();
		}
		return listeners;
	}
}
