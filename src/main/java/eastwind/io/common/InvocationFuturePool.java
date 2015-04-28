package eastwind.io.common;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InvocationFuturePool {

	private ConcurrentMap<Long, InvocationFuture<?>> InvocationFutures = new ConcurrentHashMap<Long, InvocationFuture<?>>();

	public void put(InvocationFuture<?> invocationFuture) {
		InvocationFutures.put(invocationFuture.getRequest().getId(), invocationFuture);
	}

	public InvocationFuture<?> remove(Long id) {
		return InvocationFutures.remove(id);
	}

	public ConcurrentMap<Long, InvocationFuture<?>> getInvocationFutures() {
		return InvocationFutures;
	}
}
