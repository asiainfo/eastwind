package eastwind.io.transport;

import eastwind.io.model.Host;

public interface ServerTransportVisitor {

	public boolean oneOff();
	
	public ServerTransport next(Host host);
	
}
