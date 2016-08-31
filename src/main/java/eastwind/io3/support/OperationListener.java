package eastwind.io3.support;

import java.util.EventListener;

public interface OperationListener<T> extends EventListener {

	public void complete(T t);
	
}
