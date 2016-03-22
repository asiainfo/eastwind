package eastwind.io3;



public class TransportPromise extends ExecutableFuture<Transport> {

	private Transport transport;
	
	public TransportPromise(Transport transport) {
		this.transport = transport;
	}

	public void addListener(final OperationListener<TransportPromise> listener) {
		super.addListener(new Runnable() {
			@Override
			public void run() {
				listener.operationComplete(TransportPromise.this);
			}
		}, GlobalExecutor.EVENT_EXECUTOR);
	}
	
	public Transport getTransport() {
		return transport;
	}

	public void succeeded() {
		super.set(transport);
	}

	public void failed(Throwable th) {
		super.setException(th);
	}
}
