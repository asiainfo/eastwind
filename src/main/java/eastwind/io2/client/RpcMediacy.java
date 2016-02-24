package eastwind.io2.client;

import eastwind.io2.Response;

public class RpcMediacy {

	private volatile int state;
	private Throwable th;
	private Response response;
	private Rpc<?> rpc;

	public RpcMediacy(Rpc<?> rpc) {
		this.rpc = rpc;
	}

	public Rpc<?> getRpc() {
		return rpc;
	}

	public void clear() {
		state = 0;
		th = null;
		response = null;
	}

	public Throwable getTh() {
		return th;
	}

	public int getState() {
		return state;
	}

	public synchronized void setFlushed() {
		this.state = 1;
	}

	public synchronized void setTh(Throwable th) {
		this.th = th;
		this.state = 2;
		this.notifyAll();
	}

	public Response getResponse() {
		return response;
	}

	public synchronized void setResponse(Response response) {
		this.response = response;
		this.state = 3;
		this.notifyAll();
	}

	public synchronized void sync(long mil) {
		if (state != 0) {
			return;
		}
		try {
			wait(mil);
		} catch (InterruptedException e) {
		}
	}
}
