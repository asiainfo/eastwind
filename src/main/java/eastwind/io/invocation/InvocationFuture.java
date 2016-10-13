package eastwind.io.invocation;

import java.util.concurrent.Future;

public interface InvocationFuture<V> extends Future<V> {
	
	@SuppressWarnings("rawtypes")
	public static final ThreadLocal<InvocationFuture> TL = new ThreadLocal<InvocationFuture>();

	public boolean isSuccess();
	
	public boolean isThrowable();
}
