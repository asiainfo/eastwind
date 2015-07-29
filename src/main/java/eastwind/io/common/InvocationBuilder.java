package eastwind.io.common;


public class InvocationBuilder {

	@SuppressWarnings("rawtypes")
	private InvocationFuture invocationFuture = new InvocationFuture();

	public static InvocationBuilder builder() {
		InvocationBuilder invocationBuilder = new InvocationBuilder();
		InvocationFuture.INVOCATION_FUTURE_LOCAL.set(invocationBuilder.invocationFuture);
		return invocationBuilder.async();
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

	@SuppressWarnings("unchecked")
	public <T> T listenAvoid(T t, InvocationListener<Object> listener) {
		invocationFuture.addListener(listener);
		return t;
	}

	public <T> T invoke(T t) {
		return t;
	}
}
