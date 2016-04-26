package eastwind.io3;

public class TransportPromise<V> extends ListenablePromise<V> {

	protected boolean reconnect;
	protected int losts;
	protected DelayedTask pingTask;
	
	public boolean isReconnect() {
		return reconnect;
	}

	public void setReconnect(boolean reconnect) {
		this.reconnect = reconnect;
	}

	public int getLosts() {
		return losts;
	}

	public void clearLosts() {
		this.losts = 0;
	}

	public DelayedTask getPingTask() {
		return pingTask;
	}

	public void setPingTask(DelayedTask pingTask) {
		this.pingTask = pingTask;
	}
}
