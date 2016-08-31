package eastwind.io3;

import java.util.Map;

import com.google.common.collect.Maps;

public class TransmitSustainer {

	@SuppressWarnings("rawtypes")
	private Map<Long, TransmitPromise> transmits = Maps.newConcurrentMap();
	
	@SuppressWarnings("rawtypes")
	public void add(TransmitPromise lp) {
		this.transmits.put(lp.getId(), lp);
	}

	@SuppressWarnings("rawtypes")
	public TransmitPromise remove(Long id) {
		return this.transmits.remove(id);
	}
}
