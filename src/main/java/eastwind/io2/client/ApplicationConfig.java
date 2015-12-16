package eastwind.io2.client;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import eastwind.io.common.Host;

public class ApplicationConfig {

	private String name;
	private List<Host> hosts = new CopyOnWriteArrayList<Host>();
	private ClientHandshake handshake;

	public ApplicationConfig(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void addHost(Host host) {
		this.hosts.add(host);
	}

	public void addHosts(List<Host> hosts) {
		this.hosts.addAll(hosts);
	}

	public List<Host> getHosts() {
		return hosts;
	}

	public ClientHandshake getHandshake() {
		return handshake;
	}

	public void setHandshake(ClientHandshake handshake) {
		this.handshake = handshake;
	}

}
