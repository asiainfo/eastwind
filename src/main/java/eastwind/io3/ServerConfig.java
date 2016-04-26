package eastwind.io3;

import eastwind.io.common.Host;

public class ServerConfig {

	private int id;
	private String group;
	private Host host;

	public ServerConfig(int id, String group, Host host) {
		this.id = id;
		this.group = group;
		this.host = host;
	}

	public int getId() {
		return id;
	}

	public String getGroup() {
		return group;
	}

	public Host getHost() {
		return host;
	}

}
