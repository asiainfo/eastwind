package eastwind.io;

import io.netty.channel.Channel;

import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.util.Map;

import com.google.common.collect.Maps;

import eastwind.io.common.CommonUtils;

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

	public Session(int id, Channel channel) {
		this.id = id;
		if (channel != null) {
			setChannel(channel);
		}
	}

	public void setChannel(Channel channel) {
		this.lastAccessedTime = CommonUtils.currentTimeSeconds();
		if (this.channel != null) {
			this.channel = new WeakReference<Channel>(channel);
			ChannelAttr.set(channel, ChannelAttr.SESSION, this);
		}
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

	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String name, Class<T> cls) {
		if (attributes == null) {
			return null;
		}
		return (T) attributes.get(name);
	}

	public boolean isActive() {
		Channel channel = getChannel();
		return channel != null && channel.isActive();
	}

	public void setAttribute(String name, Object value) {
		if (attributes == null) {
			attributes = Maps.newHashMap();
		}
		attributes.put(name, value);
	}

	public void setAttributes(Map<String, Object> attributes) {
		if (attributes != null) {
			if (this.attributes == null) {
				this.attributes = Maps.newHashMap();
			}
			this.attributes.putAll(attributes);
		}
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
