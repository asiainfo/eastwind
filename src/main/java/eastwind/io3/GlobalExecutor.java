package eastwind.io3;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GlobalExecutor {

	public static ExecutorService EVENT_EXECUTOR = Executors.newFixedThreadPool(2);
	
}
