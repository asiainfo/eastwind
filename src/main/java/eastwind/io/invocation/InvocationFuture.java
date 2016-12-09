package eastwind.io.invocation;

import java.util.concurrent.Future;

public interface InvocationFuture<V> extends Future<V> {
	
	V getResult();
	
	InvocationInfo getInvocationInfo();
	
	boolean isSuccess();
	
	boolean isThrowable();
	
	void addListener(InvocationListener<V> listener);
}
