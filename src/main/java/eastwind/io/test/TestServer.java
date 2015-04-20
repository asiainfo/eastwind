package eastwind.io.test;

import io.netty.channel.Channel;

import java.io.IOException;
import java.util.Map;

import eastwind.io.server.EastWindServer;
import eastwind.io.server.ServerHandshaker;

public class TestServer {

	private String app = "test-server";

	private EastWindServer cioServer;

	public TestServer() {
		cioServer = new EastWindServer(app);
		cioServer.setCheckPing(false);
		cioServer.setParentThreads(1);
		cioServer.setPort(19999);
		cioServer.setServerHandshaker(new ServerHandshaker() {
			@Override
			public void prepare(Channel channel, Map<String, Object> out) {
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
