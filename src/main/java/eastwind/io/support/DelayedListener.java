package eastwind.io.support;

import java.util.EventListener;

public interface DelayedListener<T> extends EventListener {

	void timeUp(T t, DelayedExecutor executor);
}
