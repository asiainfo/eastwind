package eastwind.io.test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;

import eastwind.io.common.Host;
import eastwind.io.common.InvocationBuilder;
import eastwind.io.common.InvocationListener;
import eastwind.io.nioclient.EastWindClient;

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

	public static void main(String[] args) throws IOException, InterruptedException {
		final String remoteApp = "test-server";
		TestClient testClient = new TestClient();
		EastWindClient eastWindClient = testClient.getEastWindClient();
		Host host = new Host("127.0.0.1", 12468);
		List<Host> hosts = Lists.newArrayList(host);
		eastWindClient.createProviderGroup(remoteApp, hosts, null);
		HelloProvider helloProvider = eastWindClient.getProvider(remoteApp, HelloProvider.class);
		InvocationBuilder.builder().async().listen(helloProvider.hello("eastwind1"), new InvocationListener<String>() {
			@Override
			public void operationComplete(String result, Throwable th) {
				System.out.println(result);
			}
		});
		System.out.println("----");
		TimeUnit.SECONDS.sleep(5);
		System.out.println(helloProvider.hello("eastwind2"));
		System.out.println(helloProvider.hello("eastwind3"));
		System.in.read();
	}
}
