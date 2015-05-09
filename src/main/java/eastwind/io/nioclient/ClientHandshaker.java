package eastwind.io.nioclient;

import io.netty.channel.Channel;

import java.util.Map;

public abstract class ClientHandshaker {

	public void prepare(String remoteApp, Channel channel, Map<String, Object> in, Map<String, Object> out) {

	}

	public void handshakeComplete(Channel channel, Map<String, Object> in) {

	}
}
