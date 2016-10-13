package eastwind.io.transport;

import io.netty.channel.Channel;

import java.lang.ref.WeakReference;

public class ClientTransport {

	private String id;
	private String uuid;
	private String group;
	private WeakReference<Channel> channelRef;

	public ClientTransport(String group, String uuid, Channel channel) {
		this.group = group;
		this.uuid = uuid;
		this.channelRef = new WeakReference<Channel>(channel);
		this.id = channel.id().asShortText();
	}

	public String getId() {
		return id;
	}
	
	public String getUuid() {
		return uuid;
	}

	public String getGroup() {
		return group;
	}

}
