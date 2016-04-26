package eastwind.io3;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eastwind.io.common.Host;

public class RemoteApplicationGroup implements Group {

	private String group;

	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private ReadLock readLock = lock.readLock();
	private WriteLock writeLock = lock.writeLock();

	private ArrayList<RemoteApplication> applications = Lists.newArrayList();
	private Map<Class<?>, Object> invokers = Maps.newHashMap();

	private int i = 0;

	public RemoteApplicationGroup(String group) {
		this.group = group;
	}

	@Override
	public String getGroup() {
		return group;
	}

	public Object getInvoker(Class<?> interf) {
		return invokers.get(interf);
	}

	public void put(Class<?> interf, Object obj) {
		invokers.put(interf, obj);
	}

	public ServerTransport next() {
		if (applications.size() == 0) {
			return null;
		}
		if (i >= applications.size()) {
			i = 0;
		}
		int f = i;
		for (;;) {
			if (i >= applications.size()) {
				i = 0;
			}
			RemoteApplication ra = applications.get(i);
			ServerTransport st = ra.next();
			i++;
			if (st != null) {
				return st;
			}
			if (f == i) {
				return null;
			}
		}
	}

	public RemoteApplication getApplication(Host host) {
		RemoteApplication ra = null;
		readLock.lock();
		for (RemoteApplication t : applications) {
			if (t.getTransport(host) != null) {
				ra = t;
				break;
			}
		}
		readLock.unlock();
		return ra;
	}

	public RemoteApplication get(String uuid) {
		readLock.lock();
		RemoteApplication ta = findApplication(uuid);
		readLock.unlock();
		if (ta != null) {
			return ta;
		}
		writeLock.lock();
		ta = findApplication(uuid);
		if (ta != null) {
			writeLock.unlock();
			return ta;
		}

		ta = new RemoteApplication(group);
		ta.setUuid(uuid);
		applications.add(ta);
		writeLock.unlock();
		return ta;
	}

	private RemoteApplication findApplication(String uuid) {
		for (RemoteApplication ta : applications) {
			if (uuid.equals(ta.getUuid())) {
				return ta;
			}
		}
		return null;
	}
}
