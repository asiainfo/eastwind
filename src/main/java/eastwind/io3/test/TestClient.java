package eastwind.io3.test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import eastwind.io.common.Host;
import eastwind.io.test.Hello;
import eastwind.io3.ApplicationManager;
import eastwind.io3.EastwindFramework;
import eastwind.io3.OperationListener;
import eastwind.io3.ServerTransport;

public class TestClient {

	public static void main(String[] args) throws IOException, InterruptedException, NoSuchMethodException,
			SecurityException, TimeoutException, ExecutionException {
		Host host = new Host("127.0.0.1", 12468);
		EastwindFramework ef = new EastwindFramework("test-client", false);
		ef.start();
		ApplicationManager am = ef.getApplicationManager();
		// am.depend("test-server");
		// am.addConfig("test-server", new Host("127.0.0.1", 12468));
		// ef.registerMessageListener(new MessageListener<String>() {
		// @Override
		// public void onMessage(String message, Transport transport) {
		// System.out.println(message);
		// }
		// });
		ServerTransport serverTransport = am.getTransport(host);
		serverTransport.addHandshakeListener(new OperationListener<ServerTransport>() {
			@Override
			public void operationComplete(ServerTransport t) {
//				if (t.isActive()) {
					Hello hello = t.createInvoker(Hello.class);
					System.out.println(hello.hello("eastwind"));
//				}
			}
		});
		System.in.read();
	}

}
