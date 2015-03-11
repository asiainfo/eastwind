package boc.message.nioclient;

import boc.message.common.HelloProvider;
import boc.message.common.Host;
import boc.message.common.RequestFuture;

public class HelloInvoker {

	private Host host;
	private HelloProvider helloProvider;

	public HelloInvoker(Host host, HelloProvider helloProvider) {
		this.host = host;
		this.helloProvider = helloProvider;
	}

	public RequestFuture<String> ruok() {
		RequestFuture<String> rf = new RequestFuture<String>(host);
		rf.start();
		helloProvider.ruok();
		rf.end();
		return rf;
	}
}