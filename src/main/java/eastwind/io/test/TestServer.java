package eastwind.io.test;

import java.io.IOException;

import eastwind.io.EastwindServer;

public class TestServer {

	public static String TEST_SERVER = "test-server";

	public static void main(String[] args) throws IOException {
		EastwindServer eastwind = new EastwindServer(TEST_SERVER);
		eastwind.registerHandler(new HelloImpl());
		eastwind.start();
		System.in.read();
	}

}
