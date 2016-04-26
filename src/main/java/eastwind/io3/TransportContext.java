package eastwind.io3;

import java.util.Map;

import com.google.common.collect.Maps;

public class TransportContext {

	protected Application localApplication;
	protected Sequence sequence;
	protected DelayedExecutor delayedExecutor;
	@SuppressWarnings("rawtypes")
	protected Map<Long, ListenablePromise> promises = Maps.newConcurrentMap();

	public TransportContext(Application localApplication, Sequence sequence) {
		this.localApplication = localApplication;
		this.sequence = sequence;
	}

	public Application getLocalApplication() {
		return localApplication;
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

	public DelayedExecutor getDelayedExecutor() {
		return delayedExecutor;
	}
	
}
