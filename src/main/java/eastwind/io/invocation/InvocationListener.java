package eastwind.io.invocation;

import java.util.EventListener;


public interface InvocationListener<T> extends EventListener {

	void onResult(InvocationFuture<T> future);
	
	void onInvokeException(InvocationFuture<T> future);
	
	void onExecutionException(InvocationFuture<T> future);
	
	void onCanceled(InvocationFuture<T> future);
}
