package eastwind.io3;

import com.google.common.util.concurrent.AbstractFuture;

public class ExecutableFuture<V> extends AbstractFuture<V> {

	@SuppressWarnings("rawtypes")
	protected void addListener0(final OperationListener listener) {
		super.addListener(new Runnable() {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				listener.operationComplete(ExecutableFuture.this);
			}
		}, GlobalExecutor.EVENT_EXECUTOR);
	}
	
}
