package boc.message.common;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SharedScheduledExecutor {

	public static ScheduledExecutorService ses = Executors.newScheduledThreadPool(5);
	
}
