package boc.message.common;

import java.util.LinkedList;
import java.util.List;

public class RequestFuture<R> {

	public static ThreadLocal<RequestFuture<?>> REQUEST_FUTURE_LOCAL = new ThreadLocal<RequestFuture<?>>();

	private Request request;
	private Respone<R> respone;

	private long time = System.currentTimeMillis();
	private int timeout = 10000;
	private String app;
	private Host host;

	private volatile List<FutureListener<R>> futures = new LinkedList<FutureListener<R>>();

	// 1:ok -1:connet fail -2:timeout
	private int[] stat = new int[1];

	public RequestFuture() {
	}
	
	public RequestFuture<R> start() {
		REQUEST_FUTURE_LOCAL.set(this);
		return this;
	}

	public RequestFuture<R> end() {
		REQUEST_FUTURE_LOCAL.set(null);
		return this;
	}

	public void addFuture(FutureListener<R> futureListener) {
		boolean added = false;
		synchronized (this) {
			if (stat[0] == 0) {
				this.futures.add(futureListener);
				added = true;
			}
		}
		if (!added) {
			futureListener.operationComplete(this);
		}
	}

	public void done(Respone<R> respone) {
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
				futures.add(new FutureListener<R>() {
					@Override
					public void operationComplete(RequestFuture<R> rf) {
						synchronized (RequestFuture.this) {
							RequestFuture.this.notifyAll();
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

	private void invokeListener() {
		for (int i = 0; i < futures.size(); i++) {
			futures.get(i).operationComplete(this);
		}
	}

	public boolean isDone() {
		return stat[0] == 1;
	}

	public RequestFuture(Request request) {
		this.request = request;
	}

	public Request getRequest() {
		return request;
	}

	public RequestFuture<R> setRequest(Request request) {
		this.request = request;
		return this;
	}

	public Respone<R> getRespone() {
		return respone;
	}

	public void setRespone(Respone<R> respone) {
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

	public RequestFuture<R> setTimeout(int timeout) {
		this.timeout = timeout;
		return this;
	}

	public long getTime() {
		return time;
	}

	public long getId() {
		return request.getId();
	}
}
