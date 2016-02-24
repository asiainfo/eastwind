package eastwind.io2.server;

import io.netty.channel.Channel;

import java.util.List;

import com.google.common.collect.Lists;

import eastwind.io2.Handshake;

public class InboundChannelGroup {

	private String app;
	private List<InboundChannel> channels = Lists.newArrayList();

	public InboundChannelGroup(String app) {
		this.app = app;
	}

	public synchronized InboundChannel addChannel(Handshake handshake, Channel channel) {
		InboundChannel ic = getChannel(handshake.getMyUuid());
		if (ic == null) {
			ic = new InboundChannel(app, handshake.getMyUuid());
			channels.add(ic);
		}
		ic.setChannel(channel);
		return ic;
	}

	public synchronized InboundChannel getChannel(String uuid) {
		for (InboundChannel ic : channels) {
			if (ic.getUuid().equals(uuid)) {
				return ic;
			}
		}
		return null;
	}
}
