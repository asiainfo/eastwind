package eastwind.io3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eastwind.io.common.Host;

public class RemoteAppGroup implements Group {

	private String group;

	private volatile boolean dependent;
	private AtomicInteger globalIndex = new AtomicInteger();
	private CopyOnWriteArrayList<RemoteConfig> configs = Lists.newCopyOnWriteArrayList();

	private ArrayList<RemoteApp> remoteApps = Lists.newArrayList();
	private Map<Class<?>, Object> invokers = Maps.newHashMap();

	public HostTraverser hostTraverser() {
		return new HostTraverser();
	}
	
	class HostTraverser implements Iterator<Host> {

		private int i;
		private boolean started;
		private ListIterator<RemoteConfig> it;
		private int startIndex;

		public HostTraverser() {
			this.i = globalIndex.getAndIncrement();
			int n = configs.size();
			if (n == 0) {
				startIndex = -1;
				return;
			}
			if (this.i >= n) {
				this.i = i % n;
				for (;;) {
					n = configs.size();
					if (n == 0) {
						startIndex = -1;
						return;
					}
					int j = globalIndex.intValue();
					if (j < n || globalIndex.compareAndSet(j, j % n)) {
						break;
					}
				}
			}
			prepare();
		}

		private void prepare() {
			it = configs.listIterator();
			if (!it.hasNext()) {
				startIndex = -1;
				return;
			}
			for (int j = 0; j < i; j++) {
				if (it.hasNext()) {
					it.next();
				} else {
					while (it.hasPrevious()) {
						it.previous();
					}
				}
			}
			this.startIndex = it.hasNext() ? it.nextIndex() : 0;
			System.out.println("startIndex:" + startIndex);
		}

		public void first() {
			started = false;
			prepare();
		}

		@Override
		public boolean hasNext() {
			if (startIndex == -1) {
				return false;
			}
			if (started) {
				if (it.hasNext()) {
					return it.nextIndex() != startIndex;
				} else {
					return startIndex != 0;
				}
			}
			return true;
		}

		@Override
		public Host next() {
			if (startIndex == -1) {
				return null;
			}
			if (!it.hasNext()) {
				while (it.hasPrevious()) {
					it.previous();
				}
			}
			if (started && it.nextIndex() == startIndex) {
				return null;
			}
			if (!started) {
				started = true;
			}
			return it.next().getHost();
		}

		@Override
		public void remove() {
			
		}

	}

	public boolean isDependent() {
		return dependent;
	}

	public void setDependent(boolean dependent) {
		this.dependent = dependent;
	}

	public List<RemoteConfig> getConfigs() {
		return configs;
	}

	public void addConfig(RemoteConfig rc) {
		this.configs.add(rc);
	}

	public RemoteAppGroup(String group) {
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

	public RemoteApp getApp(Host host) {
		RemoteApp ra = null;
		for (RemoteApp t : remoteApps) {
			if (t.getTransport(host) != null) {
				ra = t;
				break;
			}
		}
		return ra;
	}

	public RemoteApp get(String uuid) {
		RemoteApp ra = findApp(uuid);
		if (ra != null) {
			return ra;
		}
		ra = findApp(uuid);
		if (ra != null) {
			return ra;
		}

		ra = new RemoteApp(group);
		ra.setUuid(uuid);
		remoteApps.add(ra);
		return ra;
	}

	private RemoteApp findApp(String uuid) {
		for (RemoteApp ra : remoteApps) {
			if (uuid.equals(ra.getUuid())) {
				return ra;
			}
		}
		return null;
	}
}
