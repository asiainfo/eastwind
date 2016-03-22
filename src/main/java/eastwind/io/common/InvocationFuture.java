package eastwind.io.common;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InvocationFuture<R> {

	public static ThreadLocal<InvocationFuture<?>> INVOCATION_FUTURE_LOCAL = new ThreadLocal<InvocationFuture<?>>();

	private Request request;
	private Response<R> respone;

	private long time = System.currentTimeMillis();
	private int timeout = 10000;
	private String app;
	private Host host;
	private boolean sync;
	private boolean broadcast;

	private volatile List<InvocationListener<R>> listneners;

	private List<InvocationFuture<R>> subInvocationFutures;

	// 1:ok -1:connet fail -2:timeout
	private int[] stat = new int[1];

	private synchronized List<InvocationListener<R>> getListeners() {
		if (listneners == null) {
			listneners = new LinkedList<InvocationListener<R>>();
		}
		return listneners;
	}

	private synchronized List<InvocationFuture<R>> getSubInvocationFutures0() {
		if (subInvocationFutures == null) {
			subInvocationFutures = new ArrayList<InvocationFuture<R>>(0);
		}
		return subInvocationFutures;
	}

	private void invokeListener() {
		if (this.listneners == null) {
			return;
		}
		for (int i = 0; i < getListeners().size(); i++) {
			getListeners().get(i).operationComplete(respone.getResult(), respone.getTh());
		}
	}

	public long getId() {
		return request.getId();
	}

	public void addListener(InvocationListener<R> listener) {
		if (broadcast) {
			for (InvocationFuture<R> sub : getSubInvocationFutures0()) {
				sub.addListener(listener);
			}
		} else {
			boolean added = false;
			synchronized (this) {
				if (stat[0] == 0) {
					this.getListeners().add(listener);
					added = true;
				}
			}
			if (!added) {
				listener.operationComplete(respone.getResult(), respone.getTh());
			}
		}
	}

	public List<InvocationFuture<R>> getSubInvocationFutures() {
		return subInvocationFutures;
	}

	public void addSubInvocationFuture(InvocationFuture<R> invocationFuture) {
		getSubInvocationFutures0().add(invocationFuture);
	}

	public void done(Response<R> respone) {
		synchronized (this) {
			this.stat[0] = 1;
			this.respone = respone;
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

	public R sync() {
		synchronized (this) {
			if (stat[0] == 0) {
				getListeners().add(new InvocationListener<R>() {
					@Override
					public void operationComplete(R result, Throwable th) {
						synchronized (InvocationFuture.this) {
							InvocationFuture.this.notifyAll();
						}
					}
				});

				try {
					this.wait(timeout);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		if (stat[0] == 1) {
			return respone.getResult();
		} else if (stat[0] == -2) {
			throw new RuntimeException("timeout");
		} else {
			throw new RuntimeException("host is invalid");
		}
	}

	public void setTime(long time) {
		this.time = time;
	}

	public boolean isDone() {
		return stat[0] == 1;
	}

	public boolean isSync() {
		return sync;
	}

	public void setSync(boolean sync) {
		this.sync = sync;
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public Response<R> getRespone() {
		return respone;
	}

	public void setRespone(Response<R> respone) {
		this.respone = respone;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public Host getHost() {
		return host;
	}

	public void setHost(Host host) {
		this.host = host;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public long getTime() {
		return time;
	}

	public boolean isBroadcast() {
		return broadcast;
	}

	public void setBroadcast(boolean broadcast) {
		this.broadcast = broadcast;
	}
}
