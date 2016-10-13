package eastwind.io.support;

import java.util.EventListener;

public interface OperationListener<T> extends EventListener {

	public void complete(T t);
	
}
