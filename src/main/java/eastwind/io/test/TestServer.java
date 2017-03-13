package eastwind.io.test;

import java.io.IOException;

import eastwind.io.EastWindServer;

public class TestServer {

	public static String TEST_SERVER = "test-server";

	public static void main(String[] args) throws IOException {
		EastWindServer server = new EastWindServer(TEST_SERVER);
		server.registerProvider(new FruitProviderImpl());
		server.start();
		System.in.read();
	}

}