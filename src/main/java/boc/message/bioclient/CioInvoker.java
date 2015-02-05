package boc.message.bioclient;

import boc.message.common.CioProvider;
import boc.message.common.RequestFuture;

public class CioInvoker {

	private CioProvider cioProvider;

	public CioInvoker(CioProvider cioProvider) {
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
