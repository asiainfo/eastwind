package eastwind.io.transport;

import eastwind.io.model.Host;

public interface ServerSelector {
	
	public void first();
	
	public boolean skippable();
	
	public Host next();
}
