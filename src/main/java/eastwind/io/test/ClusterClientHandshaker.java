package eastwind.io.test;

import io.netty.channel.Channel;

import java.util.Map;

import eastwind.io.common.Host;
import eastwind.io.nioclient.ClientHandshaker;

public class ClusterClientHandshaker extends ClientHandshaker {

	private String app;
	private Host localHost;

	public ClusterClientHandshaker(String app, Host localHost) {
		this.app = app;
		this.localHost = localHost;
	}

	@Override
	public void prepare(String remoteApp, Channel channel, Map<String, Object> in, Map<String, Object> out) {
		String uuid = (String) in.get("uuid");
		System.out.println(uuid);
		out.put("app", app);
		out.put("host", localHost);
	}

	@Override
	public void handshakeComplete(Channel channel, Map<String, Object> in) {
		
	}

}
