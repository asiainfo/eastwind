package boc.message.common;

import boc.message.server.Provider;

public interface HelloProvider {

	@Provider
	public String ruok();
	
}