package eastwind.io3.transport;

import eastwind.io3.obj.Host;

public interface ServerSelector {
	
	public void first();
	
	public boolean canSkip();
	
	public Host next();
}
