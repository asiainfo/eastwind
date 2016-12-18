package eastwind.io2;

import java.util.EventListener;

public interface Listener<T> extends EventListener {
	
	void listen(T t);
	
}
