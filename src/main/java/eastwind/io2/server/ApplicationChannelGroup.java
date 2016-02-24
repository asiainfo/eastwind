package eastwind.io2.server;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;

import eastwind.io2.Handshake;
import eastwind.io2.client.ApplicationConfigManager;

public class ApplicationChannelGroup {

	private ApplicationConfigManager applicationConfigManager;
	
	private ConcurrentMap<String, InboundChannelGroup> clientChannelGroup = Maps.newConcurrentMap();
	
	public InboundChannel addChannel(Handshake handshake, Channel channel) {
		return clientChannelGroup.get(handshake.getApp()).addChannel(handshake, channel);
	}
}
