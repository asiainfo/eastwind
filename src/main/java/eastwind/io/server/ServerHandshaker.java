package eastwind.io.server;

import io.netty.channel.Channel;

import java.util.Map;


public abstract class ServerHandshaker {

	public boolean isMultiStep() {
		return false;
	}

	public void prepare(Channel channel, Map<String, Object> out) {

	}

	public void handshake(String app, Channel channel, Map<String, Object> in, Map<String, Object> out) {

	}

}
