package eastwind.io3.test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import eastwind.io.common.Host;
import eastwind.io.test.Hello;
import eastwind.io3.ApplicationManager;
import eastwind.io3.EastwindFramework;

public class TestClient {

	public static void main(String[] args) throws IOException, InterruptedException, NoSuchMethodException,
			SecurityException, TimeoutException, ExecutionException {
		Host host = new Host("127.0.0.1", 12468);
		String target = "test-server";
		EastwindFramework ef = new EastwindFramework("test-client", false);
		ef.start();
		ApplicationManager am = ef.getApplicationManager();
		am.addConfig(target, host);
		Hello hello = am.createInvoker(target, Hello.class);
		System.out.println(hello.hello("baby"));
	}

}
