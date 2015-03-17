package boc.message.test;

import java.io.IOException;
import java.util.Map;

import boc.message.common.Host;
import boc.message.nioclient.CioClient;
import boc.message.nioclient.ClientHandshaker;

public class TestClient {

	private String app = "test-client";

	private CioClient cioClient;

	public TestClient() {
		cioClient = new CioClient(app);
		cioClient.start();
	}

	public CioClient getCioClient() {
		return cioClient;
	}

	public static void main(String[] args) throws IOException {
		final String remoteApp = "test-server";
		TestClient testClient = new TestClient();
		CioClient cioClient = testClient.getCioClient();
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
		HelloProvider helloProvder = cioClient.buildProvider(remoteApp, HelloProvider.class);
		HelloInvoker helloInvoker = new HelloInvoker(remoteApp, helloProvder);
		System.out.println(helloInvoker.build(new Host("127.0.0.1", 19999)).hello("baby").sync());
		System.in.read();
	}
}
