package eastwind.io2.server;

import java.util.Map;

import eastwind.io.common.Host;

public abstract class ServerHandshake {

	public abstract HandshakeResult handshake(String remoteApp, Host remoteHost, Map<Object, Object> properties);
}
