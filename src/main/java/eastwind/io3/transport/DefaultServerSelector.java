package eastwind.io3.transport;

import eastwind.io3.model.Host;

public class DefaultServerSelector implements ServerSelector {

	private HostIterator hostIterator;
	
	public DefaultServerSelector(HostIterator hostIterator) {
		this.hostIterator = hostIterator;
	}

	@Override
	public void first() {
		hostIterator.first();
	}
	
	@Override
	public boolean skippable() {
		return true;
	}

	@Override
	public Host next() {
		return hostIterator.next();
	}

}
