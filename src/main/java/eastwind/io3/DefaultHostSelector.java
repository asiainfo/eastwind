package eastwind.io3;

import eastwind.io.common.Host;
import eastwind.io3.RemoteAppGroup.HostTraverser;

public class DefaultHostSelector implements HostSelector {

	private HostTraverser hostTraverser;
	
	public DefaultHostSelector(HostTraverser hostTraverser) {
		this.hostTraverser = hostTraverser;
	}

	@Override
	public void first() {
		hostTraverser.first();
	}
	
	@Override
	public boolean canSkip() {
		return true;
	}

	@Override
	public Host next() {
		return hostTraverser.next();
	}

}
