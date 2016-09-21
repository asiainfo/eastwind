package eastwind.io3;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public class ChannelStat {

	public static AttributeKey<ChannelStat> CHANNEL_STAT = AttributeKey.valueOf("CHANNELSTAT");

	public static ChannelStat get(Channel channel) {
		ChannelStat stat = channel.attr(CHANNEL_STAT).get();
		if (stat == null) {
			stat = new ChannelStat(channel.id().asShortText());
			channel.attr(CHANNEL_STAT).set(stat);
		}
		return stat;
	}

	private String id;
	private boolean shaked;

	public ChannelStat(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public boolean isShaked() {
		return shaked;
	}

	public void setShaked(boolean shaked) {
		this.shaked = shaked;
	}

}
