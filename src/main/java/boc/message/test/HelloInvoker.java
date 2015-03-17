package boc.message.test;

import boc.message.common.RequestFuture;
import boc.message.nioclient.AbstractAsyncInvoker;

public class HelloInvoker extends AbstractAsyncInvoker<HelloInvoker, HelloProvider>{

	public HelloInvoker(String app, HelloProvider provider) {
		super(app, provider);
	}

	public RequestFuture<String> hello(String name) {
		return super.pickRequestFuture(provider.hello(name));
	}
}
