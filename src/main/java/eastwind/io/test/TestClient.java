package eastwind.io.test;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;

import eastwind.io.common.Host;
import eastwind.io.common.InvocationListener;
import eastwind.io.nioclient.EastWindClient;
import eastwind.io.nioclient.InvocationBuilder;

public class TestClient {

	private String app = "test-client";

	private EastWindClient eastWindClient;

	public TestClient() {
		eastWindClient = new EastWindClient(app);
		eastWindClient.start();
	}

	public EastWindClient getEastWindClient() {
		return eastWindClient;
	}

	public static void main(String[] args) throws IOException {
		final String remoteApp = "test-server";
		TestClient testClient = new TestClient();
		EastWindClient eastWindClient = testClient.getEastWindClient();
		List<Host> hosts = Lists.newArrayList(new Host("127.0.0.1", 12468));
		eastWindClient.createProviderGroup(remoteApp, hosts, null);
		HelloProvider helloProvider = eastWindClient.getProvider(remoteApp, HelloProvider.class);
		InvocationBuilder.builder().async().listen(helloProvider.hello("eastwind"), new InvocationListener<String>() {
			@Override
			public void operationComplete(String result, Throwable th) {
				System.out.println(result);
			}
		});
		System.out.println(111);
		System.in.read();
	}
}
