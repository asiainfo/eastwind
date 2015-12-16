package eastwind.io2.server;

import java.util.Map;

import eastwind.io.common.Host;

public class DefaultServerHandshake extends ServerHandshake {

	public static final DefaultServerHandshake INSTANCE = new DefaultServerHandshake();
	
	@Override
	public HandshakeResult handshake(String remoteApp, Host remoteHost, Map<Object, Object> properties) {
		return null;
	}

}
