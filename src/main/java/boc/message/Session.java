package boc.message;

import io.netty.channel.Channel;

import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import boc.message.common.CioUtils;

import com.google.common.collect.Maps;

public class Session {

	private static ThreadLocal<Session> TL = new ThreadLocal<Session>();

	private AtomicInteger nextId = new AtomicInteger();

	private int id = nextId.getAndIncrement();
	
	private long lastCreationTime = CioUtils.currentTimeSeconds();
	private long lastAccessedTime = CioUtils.currentTimeSeconds();

	private WeakReference<Channel> channel;
	private Map<String, Object> attributes;

	public static Session getSession() {
		return TL.get();
	}

	public static void setSession(Session session) {
		TL.set(session);
	}

	public Session(Channel channel) {
		this.channel = new WeakReference<Channel>(channel);
	}

	public int getId() {
		return id;
	}

	public Channel getChannel() {
		return channel.get();
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

	public long getLastCreationTime() {
		return lastCreationTime;
	}

	public long getLastAccessedTime() {
		return lastAccessedTime;
	}
}
