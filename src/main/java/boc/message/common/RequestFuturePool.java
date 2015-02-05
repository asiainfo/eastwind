package boc.message.common;

import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;

public class RequestFuturePool {

	private ConcurrentMap<Long, RequestFuture<?>> requestFutures = Maps.newConcurrentMap();

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
