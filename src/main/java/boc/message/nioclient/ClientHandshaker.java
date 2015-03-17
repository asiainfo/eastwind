package boc.message.nioclient;

import java.util.Map;

public abstract class ClientHandshaker {

	public abstract String getName();

	public void prepare(Map<String, Object> in, Map<String, Object> out) {

	}

	public void handshakeComplete(Map<String, Object> in) {

	}
}
