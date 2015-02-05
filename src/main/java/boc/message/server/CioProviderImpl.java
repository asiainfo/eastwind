package boc.message.server;

import boc.message.common.CioProvider;

public class CioProviderImpl implements CioProvider {

	@Override
	public String ruok() {
		return "#imok";
	}
}
