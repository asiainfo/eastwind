package eastwind.io.transport;

import eastwind.io.model.Host;

public interface ServerTransportVisitor {

	boolean oneOff();
	
	ServerTransport next(Host host);
	
}
