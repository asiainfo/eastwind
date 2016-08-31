package eastwind.io3;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public class ChannelStat {

	public static AttributeKey<ChannelStat> CHANNEL_STAT = AttributeKey.valueOf("CHANNELSTAT");

	public static ChannelStat get(Channel channel) {
		ChannelStat cs = channel.attr(CHANNEL_STAT).get();
		if (cs == null) {
			cs = new ChannelStat(channel.id().asShortText());
			channel.attr(CHANNEL_STAT).set(cs);
		}
		return cs;
	}

	private String id;
	private boolean shaked;
	private boolean server;

	public ChannelStat(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public boolean isServer() {
		return server;
	}

	public boolean isClient() {
		return !server;
	}

	public boolean isShaked() {
		return shaked;
	}

	public void setServer(boolean server) {
		this.server = server;
	}

	public void setShaked(boolean shaked) {
		this.shaked = shaked;
	}

}
