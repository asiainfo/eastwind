package eastwind.io2.client;

import java.util.Map;

import eastwind.io.common.Host;

public class DefaultClientHandshake extends ClientHandshake {
	
	public static final DefaultClientHandshake INSTANCE = new DefaultClientHandshake();
	
	@Override
	protected Map<Object, Object> prepare() {
		return null;
	}

	@Override
	protected void complete(boolean result, String remoteApp, Host remoteHost, Object property) {
	}
}
