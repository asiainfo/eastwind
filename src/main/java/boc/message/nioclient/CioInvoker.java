package boc.message.nioclient;

import boc.message.common.CioProvider;
import boc.message.common.Host;
import boc.message.common.RequestFuture;

public class CioInvoker {

	private Host host;
	private CioProvider cioProvider;

	public CioInvoker(Host host, CioProvider cioProvider) {
		this.host = host;
		this.cioProvider = cioProvider;
	}

	public RequestFuture<String> ruok() {
		RequestFuture<String> rf = new RequestFuture<String>(host);
		rf.start();
		cioProvider.ruok();
		rf.end();
		return rf;
	}
}