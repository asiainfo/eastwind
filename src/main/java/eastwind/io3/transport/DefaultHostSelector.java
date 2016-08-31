package eastwind.io3.transport;

import eastwind.io3.obj.Host;

public class DefaultHostSelector implements HostSelector {

	private HostVisitor hostTraverser;
	
	public DefaultHostSelector(HostVisitor hostTraverser) {
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
