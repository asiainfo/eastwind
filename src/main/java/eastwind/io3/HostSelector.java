package eastwind.io3;

import eastwind.io.common.Host;

public interface HostSelector {
	
	public void first();
	
	public boolean canSkip();
	
	public Host next();
}
