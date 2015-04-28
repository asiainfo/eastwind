package eastwind.io.nioclient;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eastwind.io.common.Host;

public class ProviderGroup {

	private int i = 0;

	private String app;

	private ClientHandshaker clientHandshaker;
	private List<Host> hosts = new CopyOnWriteArrayList<Host>();
	private List<Object> providers = Lists.newLinkedList();

	public ProviderGroup(String app, List<Host> hosts, ClientHandshaker clientHandshaker) {
		this.app = app;
		this.hosts.addAll(hosts);
		this.clientHandshaker = clientHandshaker;
	}

	public String getApp() {
		return app;
	}

	public Host nextHost() {
		return hosts.get(0);
	}

	public Object getProvider(Class<?> interf) {
		for (int i = 0; i < providers.size(); i++) {
			if (interf.isInstance(providers.get(i))) {
				return providers.get(i);
			}
		}
		return null;
	}

	public void addProvider(Class<?> interf, Object provider) {
		synchronized (providers) {
			for (int i = 0; i < providers.size(); i++) {
				if (interf.isInstance(providers.get(i))) {
					return;
				}
			}
			providers.add(provider);
		}
	}

	public ClientHandshaker getClientHandshaker() {
		return clientHandshaker;
	}

	public boolean contain(Host host) {
		synchronized (hosts) {
			return hosts.contains(host);
		}
	}

	public void addHost(Host host) {
		synchronized (hosts) {
			if (!contain(host)) {
				this.hosts.add(host);
			}
		}
	}

	public void addHosts(List<Host> hosts) {
		synchronized (this.hosts) {
			Set<Host> added = Sets.newHashSet();
			for (Host h : hosts) {
				if (!contain(h)) {
					added.add(h);
				}
			}
			this.hosts.addAll(added);
		}
	}

	public List<Host> getHosts() {
		return hosts;
	}

}
