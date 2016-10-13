package eastwind.io.test;

import java.io.IOException;

import eastwind.io.EastwindClient;
import eastwind.io.model.Host;

public class TestClient {

	public static String TEST_CLIENT = "test-client";
	
	public static void main(String[] args) throws IOException, NoSuchMethodException, SecurityException {
		EastwindClient client = new EastwindClient(TEST_CLIENT);
		client.addServer(TestServer.TEST_SERVER, new Host("127.0.0.1", 12468));
		client.start();
		Hello hello = client.createInvokerOnBinary(TestServer.TEST_SERVER, Hello.class);
		System.out.println(hello.hello("eastwind"));
		System.in.read();
	}
	
}
