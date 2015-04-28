package eastwind.io.nioclient;

import eastwind.io.common.Host;
import eastwind.io.common.InvocationFuture;
import eastwind.io.common.InvocationListener;

public class InvocationBuilder {

	@SuppressWarnings("rawtypes")
	private InvocationFuture invocationFuture = new InvocationFuture();

	public static InvocationBuilder builder() {
		InvocationBuilder invocationBuilder = new InvocationBuilder();
		InvocationFuture.INVOCATION_FUTURE_LOCAL.set(invocationBuilder.invocationFuture);
		return invocationBuilder;
	}

	public InvocationBuilder broadcast() {
		invocationFuture.setBroadcast(true);
		return this;
	}

	public InvocationBuilder sync() {
		invocationFuture.setSync(true);
		return this;
	}

	public InvocationBuilder async() {
		invocationFuture.setSync(false);
		return this;
	}

	public InvocationBuilder host(Host host) {
		invocationFuture.setHost(host);
		return this;
	}

	@SuppressWarnings("unchecked")
	public <R> R listen(R r, InvocationListener<R> listener) {
		invocationFuture.addListener(listener);
		return r;
	}

	public <T> T invoke(T t) {
		return t;
	}
}
