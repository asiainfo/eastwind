package eastwind.io;

import java.util.Map;

import com.google.common.collect.Maps;

public class TransmitSustainer {

	@SuppressWarnings("rawtypes")
	private Map<Long, TransmitPromise> transmits = Maps.newConcurrentMap();
	
	@SuppressWarnings("rawtypes")
	public void add(TransmitPromise promise) {
		this.transmits.put(promise.getId(), promise);
	}

	@SuppressWarnings("rawtypes")
	public TransmitPromise get(Long id) {
		return this.transmits.get(id);
	}

	@SuppressWarnings("rawtypes")
	public TransmitPromise remove(Long id) {
		return this.transmits.remove(id);
	}
}
