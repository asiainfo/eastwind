package eastwind.io.invocation;

public class InvocationListenerAdapter<T> implements InvocationListener<T> {

	@Override
	public void onSuccess(InvocationFuture<T> future) {
	}

	@Override
	public void onInvokeException(InvocationFuture<T> future) {
	}

	@Override
	public void onExecutionException(InvocationFuture<T> future) {
	}

	@Override
	public void onCanceled(InvocationFuture<T> future) {
	}

}
