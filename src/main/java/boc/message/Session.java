package boc.message;

import io.netty.channel.Channel;

import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.util.Map;

import boc.message.common.CommonUtils;

import com.google.common.collect.Maps;

public class Session {

	private static ThreadLocal<Session> TL = new ThreadLocal<Session>();

	private int id;

	private long lastCreationTime = CommonUtils.currentTimeSeconds();
	private long lastAccessedTime = CommonUtils.currentTimeSeconds();

	private WeakReference<Channel> channel;
	private Map<String, Object> attributes;

	public static Session getSession() {
		return TL.get();
	}

	public static void setSession(Session session) {
		TL.set(session);
	}

	public Session(int id) {
		this.id = id;
	}

	public void setChannel(Channel channel) {
		this.channel = new WeakReference<Channel>(channel);
		ChannelAttr.set(channel, ChannelAttr.SESSION, this);
	}

	public void refreshAccessedTime() {
		this.lastAccessedTime = CommonUtils.currentTimeSeconds();
	}

	public InetSocketAddress getRemoteAddress() {
		if (channel.get() != null) {
			return (InetSocketAddress) channel.get().remoteAddress();
		}
		return null;
	}

	public Object getAttribute(String name) {
		return attributes == null ? null : attributes.get(name);
	}

	public void setAttribute(String name, Object value) {
		if (attributes == null) {
			attributes = Maps.newHashMap();
		}
		attributes.put(name, value);
	}

	public int getId() {
		return id;
	}

	public Channel getChannel() {
		return channel.get();
	}

	public long getLastCreationTime() {
		return lastCreationTime;
	}

	public long getLastAccessedTime() {
		return lastAccessedTime;
	}

}
