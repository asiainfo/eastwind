package eastwind.io.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import eastwind.io.ChannelAttr;
import eastwind.io.Session;
import eastwind.io.common.CommonUtils;
import eastwind.io.common.ScheduledExecutor;

public class SessionGroup {

	private String name;

	private int timeout = 60 * 5;

	private MultipleMap<Integer, Session> sessions = MultipleMap.newMultipleMap();

	private ChannelFutureListener suspendedListener = new ChannelFutureListener() {
		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			Session session = ChannelAttr.get(future.channel(), ChannelAttr.SESSION);
			if (session.getChannel() == future.channel()) {
				for (SessionListener listener : listeners) {
					listener.suspended(session);
				}
			}
		}
	};

	private CopyOnWriteArrayList<SessionListener> listeners = new CopyOnWriteArrayList<SessionListener>();

	public static SessionGroup build(String name, int timeout) {
		SessionGroup sessionGroup = new SessionGroup();
		sessionGroup.name = name;
		sessionGroup.timeout = timeout;
		sessionGroup.new TimeoutScanner().begin();
		return sessionGroup;
	}

	private SessionGroup() {
	}

	public Session get(Integer id) {
		return sessions.get(id);
	}

	public boolean isActive(int uid) {
		Session session = sessions.get(uid);
		if (session == null) {
			return false;
		}
		return session.isActive();
	}

	public Session createSession(int id, Map<String, Object> attributes, Channel channel) {
		Session session = sessions.get(id);
		if (session == null) {
			session = new Session(id, channel);
			session.setAttributes(attributes);
			if (sessions.putIfAbsent(id, session) == session) {
				for (SessionListener listener : listeners) {
					listener.created(session);
				}
			}
		} else {
			session.setAttributes(attributes);
			session.setChannel(channel);
			for (SessionListener listener : listeners) {
				listener.recreated(session);
			}
		}
		if (channel != null) {
			ChannelAttr.setId(channel, id);
			channel.closeFuture().addListener(suspendedListener);
		}
		return sessions.get(id);
	}

	public Iterator<Session> iterator() {
		return sessions.iterator();
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getName() {
		return name;
	}

	class TimeoutScanner extends MultipleMapScanner<Integer, Session> {

		private long expireTime;

		public TimeoutScanner() {
			super(timeout * 1000, sessions, ScheduledExecutor.ses);
		}

		@Override
		protected void prepare() {
			expireTime = CommonUtils.currentTimeSeconds();
		}

		@Override
		protected void process(Session session) {
			if (session.getLastAccessedTime() < expireTime) {
				sessions.remove(session.getId());
				for (SessionListener listener : listeners) {
					listener.destroyed(session);
				}
			}
		}

	}
}
