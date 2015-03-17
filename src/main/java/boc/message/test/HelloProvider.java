package boc.message.test;

import boc.message.server.Provider;

public interface HelloProvider {

	@Provider
	public String hello(String name);
	
}
