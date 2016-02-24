package eastwind.io3;

import io.netty.channel.ChannelPromise;
import eastwind.io.common.Host;

public class CallableApplication extends GenericApplication implements ChannelAware {

	protected Host host;
	protected ChannelPromise channelPromise;

	public ChannelPromise getChannelPromise() {
		return channelPromise;
	}

	public void setChannelPromise(ChannelPromise channelPromise) {
		this.channelPromise = channelPromise;
	}

	public Host getHost() {
		return host;
	}

	public MessageBuilder schematicMessageBuilder() {
		return new MessageBuilder();
	}
}
