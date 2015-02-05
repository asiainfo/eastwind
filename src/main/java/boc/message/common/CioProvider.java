package boc.message.common;

import boc.message.server.Provider;

public interface CioProvider {

	@Provider("#ruok")
	public String ruok();
	
}