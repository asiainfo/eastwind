package eastwind.io3;

public class TransportPromise<V> extends ListenablePromise<V> {

	protected boolean retry;
	protected int nrcy;
	protected int losts;
	protected DelayedTask checkTask;
	protected Transport transport;
	
	public TransportPromise(Transport transport) {
		this.transport = transport;
	}

	public boolean isRetry() {
		return retry;
	}

	public void setRetry(boolean retry) {
		this.retry = retry;
	}

	public void lost() {
		this.losts++;
	}

	public int getLosts() {
		return losts;
	}

	public void clearLosts() {
		this.losts = 0;
	}

	public DelayedTask getCheckTask() {
		return checkTask;
	}

	public void setCheckTask(DelayedTask checkTask) {
		this.checkTask = checkTask;
	}

	public Transport getTransport() {
		return transport;
	}

}
