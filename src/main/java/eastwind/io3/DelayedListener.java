package eastwind.io3;

import java.util.EventListener;

public interface DelayedListener<T> extends EventListener {

	public String type();
	
	public void timeUp(T t, DelayedExecutor executor);
}
