package eastwind.io3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eastwind.io.common.Host;

public class ApplicationGroup implements Group {

	private String group;

	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private ReadLock readLock = lock.readLock();
	private WriteLock writeLock = lock.writeLock();

	private volatile boolean dependent;
	private List<ServerConfig> configs = Lists.newArrayList();
	
	private ArrayList<ServerTransport> transports = Lists.newArrayList();
	private ArrayList<RemoteApplication> applications = Lists.newArrayList();
	private Map<Class<?>, Object> invokers = Maps.newHashMap();

	private int i = 0;

	public boolean isDependent() {
		return dependent;
	}

	public void setDependent(boolean dependent) {
		this.dependent = dependent;
	}

	public List<ServerConfig> getConfigs() {
		return configs;
	}

	public void addConfig(ServerConfig sc) {
		this.configs.add(sc);
	}
	
	public ApplicationGroup(String group) {
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

	public void add(ServerTransport transport) {
		this.transports.add(transport);
	}

	public ServerTransport next() {
		if (transports.size() == 0) {
			return null;
		}
		for (int j = 0; j <= transports.size(); j++) {
			i++;
			if (i >= transports.size()) {
				i = 0;
			}
			ServerTransport st = transports.get(i);
			if (st.isReady()) {
				return st;
			}
		}
		return null;
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

	public ServerTransport getTransport(Host host) {
		for (ServerTransport st : transports) {
			if (st.getServerConfig().getHost().equals(host)) {
				return st;
			}
		}
		return null;
	}
	
	public ArrayList<ServerTransport> getTransports() {
		return transports;
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
