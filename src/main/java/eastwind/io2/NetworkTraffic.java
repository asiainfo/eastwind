package eastwind.io2;

public abstract class NetworkTraffic {

	protected NetworkTrafficTransport transport;

	public NetworkTrafficTransport getTransport() {
		return transport;
	}

	public void setTransport(NetworkTrafficTransport transport) {
		this.transport = transport;
	}
	
	public void light() {
		this.transport = null;
	}
}
