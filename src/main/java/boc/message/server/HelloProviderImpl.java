package boc.message.server;

import boc.message.common.HelloProvider;

public class HelloProviderImpl implements HelloProvider {

	@Override
	public String ruok() {
		return "imok";
	}
}
