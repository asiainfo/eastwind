package eastwind.io.common;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ScheduledExecutor {

	public static ScheduledExecutorService ses = Executors.newScheduledThreadPool(5, new NamedThreadFactory(
			"ScheduledExecutor"));

}
