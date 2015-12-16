package eastwind.io2.client;

import eastwind.io.common.Host;

public class RpcBuilder {

	@SuppressWarnings("rawtypes")
	private RpcContext rpcContext = new RpcContext();

	public static RpcBuilder build() {
		RpcBuilder rb = new RpcBuilder();
		RpcContext.LOCAL.set(rb.rpcContext);
		return rb.sync();
	}

	public RpcBuilder sync() {
		rpcContext.setSync(true);
		return this;
	}

	public RpcBuilder async() {
		rpcContext.setSync(false);
		return this;
	}

	public RpcBuilder host(Host host) {
		rpcContext.setHost(host);
		return this;
	}

	@SuppressWarnings("unchecked")
	public <R> R addListener(R r, ResultListener<R> listener) {
		rpcContext.addListener(listener);
		return r;
	}

	@SuppressWarnings("unchecked")
	public <T> T addVoidListener(T t, VoidListener listener) {
		rpcContext.addListener(listener);
		return t;
	}
}
