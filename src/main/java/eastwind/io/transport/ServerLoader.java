package eastwind.io.transport;

import java.util.ArrayList;
import java.util.List;

import eastwind.io.support.GlobalExecutor;

public abstract class ServerLoader {

	private ArrayList<LoaderListener> listeners = new ArrayList<LoaderListener>();
	protected volatile boolean ready;

	public abstract List<Node> getNodes(String group);

	public abstract int getMod(String group);

	public synchronized final void ready() {
		ready = true;
		GlobalExecutor.EVENT_EXECUTOR.execute(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < listeners.size(); i++) {
					listeners.get(i).ready();
				}
			}
		});
	}

	public synchronized final void addListener(final LoaderListener listener) {
		this.listeners.add(listener);
		if (ready) {
			GlobalExecutor.EVENT_EXECUTOR.execute(new Runnable() {
				@Override
				public void run() {
					listener.ready();
				}
			});
		}
	}

}
