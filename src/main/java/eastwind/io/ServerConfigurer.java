package eastwind.io;

import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Maps;

import eastwind.io.model.Host;
import eastwind.io.support.InnerUtils;
import eastwind.io.transport.HostIterator;

public class ServerConfigurer {

	private ConcurrentMap<String, GroupConfig> servers = Maps.newConcurrentMap();
	
	public void addHost(String group, Host host) {
		GroupConfig gc = getGroupConfig(group);
		gc.hosts.add(host);
	}
	
	public void addHosts(String group, List<Host> host) {
		GroupConfig gc = getGroupConfig(group);
		gc.hosts.addAll(host);
	}
	
	public HostIterator getHostIterator(String group) {
		return new DefaultHostIterator(getGroupConfig(group));
	}
	
	private GroupConfig getGroupConfig(String group) {
		GroupConfig gc = servers.get(group);
		if (gc == null) {
			gc = InnerUtils.putIfAbsent(servers, group, new GroupConfig(group));
		}
		return gc;
	}
	
	class DefaultHostIterator implements HostIterator {
		private int i;
		private boolean started;
		private GroupConfig gc;
		private ListIterator<Host> it;
		private int startIndex;

		public DefaultHostIterator(GroupConfig gc) {
			this.gc = gc;
			this.i = gc.globalIterateIndex.getAndIncrement();
			int n = gc.hosts.size();
			if (n == 0) {
				startIndex = -1;
				return;
			}
			if (this.i >= n) {
				this.i = i % n;
				for (;;) {
					n = gc.hosts.size();
					if (n == 0) {
						startIndex = -1;
						return;
					}
					int j = gc.globalIterateIndex.intValue();
					if (j < n || gc.globalIterateIndex.compareAndSet(j, j % n)) {
						break;
					}
				}
			}
			prepare();
		}

		private void prepare() {
			it = gc.hosts.listIterator();
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
			return it.next();
		}

		@Override
		public void remove() {
			
		}
	}
	
	static class GroupConfig {
		AtomicInteger globalIterateIndex = new AtomicInteger();
		String group;
		CopyOnWriteArrayList<Host> hosts = new CopyOnWriteArrayList<Host>();

		public GroupConfig(String group) {
			this.group = group;
		}

	}

}
