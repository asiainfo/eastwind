package boc.message.server;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import boc.message.Session;
import boc.message.common.CommonUtils;
import boc.message.common.SharedScheduledExecutor;

public class SessionGroup {

	private String name;

	private int timeout = 60;

	private MultipleMap<Integer, Session> sessions = MultipleMap.newMultipleMap();

	private CopyOnWriteArrayList<SessionListener> listeners = new CopyOnWriteArrayList<SessionListener>();

	public static SessionGroup build(String name) {
		SessionGroup sessionGroup = new SessionGroup();
		sessionGroup.name = name;
		SharedScheduledExecutor.ses.schedule(sessionGroup.new TimeoutScanner(), sessionGroup.timeout * 1000,
				TimeUnit.MILLISECONDS);
		return sessionGroup;
	}

	public Session get(Integer id) {
		return sessions.get(id);
	}

	public void put(Session session) {
		sessions.put(session.getId(), session);
		for (SessionListener listener : listeners) {
			listener.created(session);
		}
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

	private class TimeoutScanner implements Runnable {

		private int shard = 0;

		@Override
		public void run() {
			long t1 = System.currentTimeMillis();
			ConcurrentMap<Integer, Session> map = sessions.getShard(shard);
			long t = CommonUtils.currentTimeSeconds() - timeout;
			for (Session session : map.values()) {
				if (session.getLastAccessedTime() < t) {
					sessions.remove(session.getId());
					for (SessionListener listener : listeners) {
						listener.destroyed(session);
					}
				}
			}
			long cost = System.currentTimeMillis() - t1;
			long nextRunTime = timeout * 1000 / sessions.getShard() - cost;
			shard++;
			if (shard >= sessions.getShard()) {
				shard %= sessions.getShard();
			}
			SharedScheduledExecutor.ses.schedule(this, nextRunTime, TimeUnit.MILLISECONDS);
		}

	}
}
