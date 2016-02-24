package eastwind.io2.client;

import eastwind.io.common.Host;

public class RpcBuilder {

	@SuppressWarnings("rawtypes")
	private Rpc rpcConfig = new Rpc();

	public static RpcBuilder build() {
		RpcBuilder rb = new RpcBuilder();
		Rpc.LOCAL.set(rb.rpcConfig);
		return rb.sync();
	}

	public RpcBuilder sync() {
		rpcConfig.setSync(true);
		return this;
	}

	public RpcBuilder async() {
		rpcConfig.setSync(false);
		return this;
	}

	public RpcBuilder host(Host host) {
		rpcConfig.setHost(host);
		return this;
	}

	@SuppressWarnings("unchecked")
	public <R> R addListener(R r, ResultListener<R> listener) {
		rpcConfig.addListener(listener);
		return r;
	}

	@SuppressWarnings("unchecked")
	public <T> T addVoidListener(T t, VoidListener listener) {
		rpcConfig.addListener(listener);
		return t;
	}
}
