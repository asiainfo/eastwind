package eastwind.io.test;

import eastwind.io.EastwindClient;
import eastwind.io.invocation.JsonInvoker;
import eastwind.io.model.Host;

public class TestClient {

	public static String TEST_CLIENT = "test-client";

	public static void main(String[] args) throws Throwable {
		EastwindClient client = new EastwindClient(TEST_CLIENT);
		client.addServer(TestServer.TEST_SERVER, new Host("127.0.0.1", 12468));
		client.start();
		// Hello hello = client.createInvokerOnBinary(TestServer.TEST_SERVER,
		// Hello.class);
		// System.out.println(hello.hello("eastwind"));

		JsonInvoker<String> helloInvoker = client.createInvokerOnJson(TestServer.TEST_SERVER, "helloImpl.hello",
				String.class);
		System.out.println(helloInvoker.invoke("eastwind"));
		System.in.read();
	}

}
