package eastwind.io.nioclient;

import eastwind.io.common.Host;
import eastwind.io.common.RequestFuture;

public class AbstractAsyncInvoker<I extends AbstractAsyncInvoker<I, P>, P> {

	protected String app;
	
	protected P provider;

	public AbstractAsyncInvoker(String app, P provider) {
		this.app = app;
		this.provider = provider;
	}
	
	@SuppressWarnings("unchecked")
	public I build(Host host) {
		RequestFuture<Object> rf = new RequestFuture<Object>();
		rf.setApp(app);
		rf.setHost(host);
		RequestFuture.REQUEST_FUTURE_LOCAL.set(rf);
		return (I) this;
	}
	
	@SuppressWarnings("unchecked")
	public RequestFuture<Object> pickRequestFuture() {
		return (RequestFuture<Object>) RequestFuture.REQUEST_FUTURE_LOCAL.get();
	}
	
	@SuppressWarnings("unchecked")
	public <T> RequestFuture<T> pickRequestFuture(T t) {
		return (RequestFuture<T>) RequestFuture.REQUEST_FUTURE_LOCAL.get();
	}
}
