package eastwind.io3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import com.google.common.collect.Lists;

import eastwind.io.common.Host;

public class TransportableApplicationGroup implements Group, Activatable {

	private String group;

	private ApplicatioinActivator applicatioinActivator;

	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private ReadLock readLock = lock.readLock();
	private WriteLock writeLock = lock.writeLock();

	private ArrayList<TransportableApplication> applications = Lists.newArrayList();

	private int i = 0;
	
	public TransportableApplicationGroup(String group) {
		this.group = group;
	}

	public TransportableApplicationGroup(String group, List<Host> hosts, ApplicatioinActivator applicatioinActivator) {
		this.group = group;
		this.applicatioinActivator = applicatioinActivator;
		for (Host host : hosts) {
			applications.add(new TransportableApplication(group, host));
		}
	}

	@Override
	public String getGroup() {
		return group;
	}

	public TransportableApplication next() {
		readLock.lock();
		if (i >= applications.size()) {
			i = 0;
		}
		TransportableApplication ta = applications.get(i);
		i++;
		readLock.unlock();
		return ta;
	}
	
	public void active() {
		readLock.lock();
		for (TransportableApplication app : applications) {
			applicatioinActivator.active(app);
		}
		readLock.unlock();
	}

	public TransportableApplication getOrCreate(String uuid) {
		readLock.lock();
		TransportableApplication ta = findApplication(uuid);
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

		ta = new TransportableApplication(group);
		ta.setUuid(uuid);
		applications.add(ta);
		writeLock.unlock();
		return ta;
	}

	private TransportableApplication findApplication(String uuid) {
		for (TransportableApplication ta : applications) {
			if (ta.getUuid().equals(uuid)) {
				return ta;
			}
		}
		return null;
	}
}
