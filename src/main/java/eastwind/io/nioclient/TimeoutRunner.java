package eastwind.io.nioclient;

import java.util.Collection;

import eastwind.io.common.RequestFuture;
import eastwind.io.common.RequestFuturePool;

public class TimeoutRunner implements Runnable {

	private RequestFuturePool requestFuturePool;
	private int invokeTimeout;
	
	public TimeoutRunner(RequestFuturePool requestFuturePool, int invokeTimeout) {
		this.requestFuturePool = requestFuturePool;
		this.invokeTimeout = invokeTimeout;
	}

	@Override
	public void run() {
		long now = System.currentTimeMillis();
		Collection<RequestFuture<?>> requestFutures = requestFuturePool.getRequestFutures().values();
		for (RequestFuture<?> rf : requestFutures) {
			int timeout = rf.getTimeout() == 0 ? invokeTimeout : rf.getTimeout();
			if (now - rf.getTime() > timeout * 1000) {
				RequestFuture<?> t = requestFuturePool.remove(rf.getId());
				if (t != null) {
					t.timeout();
				}
			}
		}
	}

}
