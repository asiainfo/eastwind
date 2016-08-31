package eastwind.io3.test;

import java.io.IOException;

import eastwind.io3.EastwindFramework;

public class TestServer {

	public static String TEST_SERVER = "test-server";

	public static void main(String[] args) throws IOException {
		EastwindFramework eastwind = new EastwindFramework(TEST_SERVER, true);
		eastwind.registerHandler(new HelloImpl());
		eastwind.start();
		System.in.read();
	}

}
