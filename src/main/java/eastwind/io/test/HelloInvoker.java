package eastwind.io.test;

import eastwind.io.common.RequestFuture;
import eastwind.io.nioclient.AbstractAsyncInvoker;

public class HelloInvoker extends AbstractAsyncInvoker<HelloInvoker, HelloProvider>{

	public HelloInvoker(String app, HelloProvider provider) {
		super(app, provider);
	}

	public RequestFuture<String> hello(String name) {
		return super.pickRequestFuture(provider.hello(name));
	}
}
