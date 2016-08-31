package eastwind.io3;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import eastwind.io3.support.DelayedExecutor;
import eastwind.io3.support.NamedThreadFactory;

public class GlobalExecutor {

	public static ScheduledExecutorService SCHEDULED_EXECUTOR = Executors.newScheduledThreadPool(2, new NamedThreadFactory(
			"SCHEDULED_EXECUTOR"));
	public static ExecutorService EVENT_EXECUTOR = Executors.newFixedThreadPool(2, new NamedThreadFactory("EVENT_EXECUTOR"));

	public static DelayedExecutor DELAYED_EXECUTOR = new DelayedExecutor();
	
	static {
//		DELAYED_EXECUTOR.register(new TransmitTicker());
	}
}
