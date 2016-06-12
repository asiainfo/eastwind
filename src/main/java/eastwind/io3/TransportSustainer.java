package eastwind.io3;

import java.util.Map;

import com.google.common.collect.Maps;

public class TransportSustainer {

	protected Sequence sequence;
	@SuppressWarnings("rawtypes")
	protected Map<Long, ListenablePromise> promises = Maps.newConcurrentMap();

	public TransportSustainer(Sequence sequence) {
		this.sequence = sequence;
	}

	public Sequence getSequence() {
		return sequence;
	}

	@SuppressWarnings("rawtypes")
	public void add(ListenablePromise lp) {
		this.promises.put(lp.getId(), lp);
	}

	@SuppressWarnings("rawtypes")
	public ListenablePromise remove(Long id) {
		return this.promises.remove(id);
	}

}
