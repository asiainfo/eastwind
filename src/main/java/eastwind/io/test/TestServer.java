package eastwind.io.test;

import java.io.IOException;

import eastwind.io.EastWindServer;

public class TestServer {

	public static String TEST_SERVER = "test-server";

	public static void main(String[] args) throws IOException {
		EastWindServer eastwind = new EastWindServer(TEST_SERVER);
		eastwind.setPort(8080);
		eastwind.registerHandler(new HelloImpl());
		eastwind.start();
		System.in.read();
	}

}