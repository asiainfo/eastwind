package eastwind.io3.transport;

import eastwind.io3.obj.Host;

public interface ServerTransportVisitor {

	public boolean oneOff();
	
	public ServerTransport next(Host host);
	
}
