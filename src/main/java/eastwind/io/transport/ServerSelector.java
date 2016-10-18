package eastwind.io.transport;

import eastwind.io.model.Host;

public interface ServerSelector {
	
	void first();
	
	boolean skippable();
	
	Host next();
}
