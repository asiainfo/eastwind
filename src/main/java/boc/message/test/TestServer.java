package boc.message.test;

import java.io.IOException;

import boc.message.server.CioServer;

public class TestServer {

	private String app = "test-server";

	private CioServer cioServer;

	public TestServer() {
		cioServer = new CioServer(app);
		cioServer.setCheckPing(false);
		cioServer.setParentThreads(1);
		cioServer.setPort(19999);
//		cioServer.registerProvider(new NameServerProvider(this));

		cioServer.start();
	}

	public static void main(String[] args) throws IOException {
		new TestServer();
		System.in.read();
	}
}
