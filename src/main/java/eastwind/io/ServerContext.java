package eastwind.io;

import java.net.SocketAddress;

import eastwind.io.model.Shake;
import eastwind.io.transport.RouteGroup;

public class ServerContext {
	
	protected long time = System.currentTimeMillis();
	protected String group;
	protected String uuid;
	protected String version;
	protected SocketAddress socketAddress;
	protected Sequence sequence;
	private ProviderRegistry providerRegistry = new ProviderRegistry();
	private RouteGroup routeGroup = new RouteGroup();
	
	public ServerContext(String group, String uuid, Sequence sequence) {
		this.group = group;
		this.uuid = uuid;
		this.sequence = sequence;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getVersion() {
		return version;
	}

	public ProviderRegistry getProviderRegistry() {
		return providerRegistry;
	}

	public RouteGroup getRouteGroup() {
		return routeGroup;
	}

	public String getGroup() {
		return group;
	}

	public String getUuid() {
		return uuid;
	}

	public SocketAddress getSocketAddress() {
		return socketAddress;
	}

	public void setSocketAddress(SocketAddress socketAddress) {
		this.socketAddress = socketAddress;
	}

	public long getTime() {
		return time;
	}

	public Sequence getSequence() {
		return sequence;
	}
	
}
