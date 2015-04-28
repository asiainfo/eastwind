package eastwind.io.nioclient;

import java.util.Map;

import eastwind.io.common.Host;

public abstract class ClientHandshaker {

	public void prepare(String app, Host remoteHost, Map<String, Object> in, Map<String, Object> out) {

	}

	public void handshakeComplete(Map<String, Object> in) {

	}
}
