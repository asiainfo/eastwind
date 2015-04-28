package eastwind.io.test;

import io.netty.channel.Channel;

import java.util.Map;

import eastwind.io.common.Host;
import eastwind.io.nioclient.ChannelGuard;
import eastwind.io.server.ServerHandshaker;

public class ClusterServerHandshaker extends ServerHandshaker {

	private String uuid;
	private ChannelGuard channelGuard;

	public ClusterServerHandshaker(String uuid, ChannelGuard channelGuard) {
		this.uuid = uuid;
		this.channelGuard = channelGuard;
	}

	@Override
	public boolean isMultiStep() {
		return true;
	}

	@Override
	public void prepare(Channel channel, Map<String, Object> out) {
		out.put("uuid", uuid);
	}

	@Override
	public void handshake(String app, Channel channel, Map<String, Object> in, Map<String, Object> out) {
		Host host = (Host) in.get("host");
		channelGuard.connectNow(host);
	}

}
