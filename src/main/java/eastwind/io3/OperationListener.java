package eastwind.io3;

import java.util.EventListener;

public interface OperationListener<T> extends EventListener {

	public void operationComplete(T t);
	
}
