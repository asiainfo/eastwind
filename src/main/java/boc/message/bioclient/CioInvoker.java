package boc.message.bioclient;

import boc.message.common.HelloProvider;
import boc.message.common.RequestFuture;

public class CioInvoker {

	private HelloProvider cioProvider;

	public CioInvoker(HelloProvider cioProvider) {
		this.cioProvider = cioProvider;
	}
	
	public RequestFuture<String> ruok() {
		RequestFuture<String> rf = new RequestFuture<>();
		rf.start();
		cioProvider.ruok();
		rf.end();
		return rf;
	}
	
}
