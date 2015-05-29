package eastwind.io.test;

import java.io.IOException;

import eastwind.io.server.EastWindServer;

public class TestServer {

	private String app = "test-server";

	private EastWindServer eastWindServer;

	public TestServer() {
		eastWindServer = new EastWindServer(app);
		eastWindServer.setCheckPing(false);
		eastWindServer.setParentThreads(1);
		eastWindServer.setPort(12468);
		eastWindServer.registerProvider(new HelloProviderImpl());
		eastWindServer.start();
	}

	public static void main(String[] args) throws IOException {
		new TestServer();
		System.in.read();
	}
}
