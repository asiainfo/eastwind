package eastwind.io2.client;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RpcContextPool {

	private ConcurrentMap<Long, RpcContext<?>> rpcContexts = new ConcurrentHashMap<Long, RpcContext<?>>();

	public void put(RpcContext<?> rpcContext) {
		rpcContexts.put(rpcContext.getId(), rpcContext);
	}

	public RpcContext<?> remove(Long id) {
		return rpcContexts.remove(id);
	}

}
