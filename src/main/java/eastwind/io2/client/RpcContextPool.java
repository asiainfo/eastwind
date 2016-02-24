package eastwind.io2.client;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RpcContextPool {

	private ConcurrentMap<Long, Rpc<?>> rpcContexts = new ConcurrentHashMap<Long, Rpc<?>>();

	public void put(Rpc<?> rpcContext) {
		rpcContexts.put(rpcContext.getId(), rpcContext);
	}

	public Rpc<?> remove(Long id) {
		return rpcContexts.remove(id);
	}

}
