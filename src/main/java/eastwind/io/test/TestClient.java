package eastwind.io.test;

import java.io.IOException;
import java.util.Map;

import eastwind.io.common.Host;
import eastwind.io.nioclient.EastWindClient;
import eastwind.io.nioclient.ClientHandshaker;

public class TestClient {

	private String app = "test-client";

	private EastWindClient cioClient;

	public TestClient() {
		cioClient = new EastWindClient(app);
		cioClient.start();
	}

	public EastWindClient getCioClient() {
		return cioClient;
	}

	public static void main(String[] args) throws IOException {
		final String remoteApp = "test-server";
		TestClient testClient = new TestClient();
		EastWindClient cioClient = testClient.getCioClient();
		cioClient.addHandshaker(new ClientHandshaker() {
			@Override
			public String getName() {
				return remoteApp;
			}

			@Override
			public void handshakeComplete(Map<String, Object> in) {
				System.out.println("client handshakeComplete");
			}
		});
		HelloProvider helloProvder = cioClient.buildProvider(HelloProvider.class);
		HelloInvoker helloInvoker = new HelloInvoker(remoteApp, helloProvder);
		System.out.println(helloInvoker.build(new Host("127.0.0.1", 19999)).hello("baby").sync());
		System.in.read();
	}
}
