package eastwind.io.transport;

import eastwind.io.model.Host;

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
