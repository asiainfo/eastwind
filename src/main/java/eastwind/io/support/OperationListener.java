package eastwind.io.support;

import java.util.EventListener;

public interface OperationListener<T> extends EventListener {

	void complete(T t);
	
}
