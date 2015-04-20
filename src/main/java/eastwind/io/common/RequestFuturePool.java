package eastwind.io.common;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RequestFuturePool {

	private ConcurrentMap<Long, RequestFuture<?>> requestFutures = new ConcurrentHashMap<Long, RequestFuture<?>>();

	public void put(RequestFuture<?> requestFuture) {
		requestFutures.put(requestFuture.getRequest().getId(), requestFuture);
	}

	public RequestFuture<?> remove(Long id) {
		return requestFutures.remove(id);
	}

	public ConcurrentMap<Long, RequestFuture<?>> getRequestFutures() {
		return requestFutures;
	}
}
