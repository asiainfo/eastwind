package eastwind.io.nioclient;

import java.util.Collection;

import eastwind.io.common.InvocationFuture;
import eastwind.io.common.InvocationFuturePool;

public class TimeoutRunner implements Runnable {

	private InvocationFuturePool invocationFuturePool;
	private int invokeTimeout;
	
	public TimeoutRunner(InvocationFuturePool invocationFuturePool, int invokeTimeout) {
		this.invocationFuturePool = invocationFuturePool;
		this.invokeTimeout = invokeTimeout;
	}

	@Override
	public void run() {
		long now = System.currentTimeMillis();
		Collection<InvocationFuture<?>> invocationFutures = invocationFuturePool.getInvocationFutures().values();
		for (InvocationFuture<?> rf : invocationFutures) {
			int timeout = rf.getTimeout() == 0 ? invokeTimeout : rf.getTimeout();
			if (now - rf.getTime() > timeout * 1000) {
				InvocationFuture<?> t = invocationFuturePool.remove(rf.getId());
				if (t != null) {
					t.timeout();
				}
			}
		}
	}

}
