package eastwind.io3.transport;

import eastwind.io3.model.Host;

public interface ServerSelector {
	
	public void first();
	
	public boolean skippable();
	
	public Host next();
}
