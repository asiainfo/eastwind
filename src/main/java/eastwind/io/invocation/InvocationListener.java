package eastwind.io.invocation;

import java.util.EventListener;


public interface InvocationListener<T> extends EventListener {

	public void onResult(InvocationFuture<T> future);
	
	public void onInvokeException(InvocationFuture<T> future);
	
	public void onExecutionException(InvocationFuture<T> future);
	
	public void onCanceled(InvocationFuture<T> future);
}
