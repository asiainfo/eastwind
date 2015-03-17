package boc.message.test;

import java.io.IOException;
import java.util.Map;

import boc.message.server.CioServer;
import boc.message.server.ServerHandshaker;

public class TestServer {

	private String app = "test-server";

	private CioServer cioServer;

	public TestServer() {
		cioServer = new CioServer(app);
		cioServer.setCheckPing(false);
		cioServer.setParentThreads(1);
		cioServer.setPort(19999);
		cioServer.setServerHandshaker(new ServerHandshaker() {
			@Override
			public void prepare(Map<String, Object> out) {
				System.out.println("server prepare");
			}
		});
		cioServer.registerProvider(new HelloProviderImpl());
		cioServer.start();
	}

	public static void main(String[] args) throws IOException {
		new TestServer();
		System.in.read();
	}
}
