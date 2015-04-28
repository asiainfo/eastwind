package eastwind.io.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import eastwind.io.common.Host;
import eastwind.io.common.InvocationListener;
import eastwind.io.nioclient.EastWindClient;
import eastwind.io.nioclient.InvocationBuilder;
import eastwind.io.server.EastWindServer;

public class TestCluster0 {

	private int id;

	private String uuid = UUID.randomUUID().toString();

	private String app = "test-cluster";

	private EastWindClient eastWindClient;
	private EastWindServer eastWindServer;

	private HelloProvider helloProvider;

	private Host host;
	private List<Host> hosts;

	public TestCluster0(int id) {
		this.id = id;
	}

	public void start() {
		eastWindClient = new EastWindClient(app);
		eastWindClient.start();
		eastWindClient.createProviderGroup(app, hosts, new ClusterClientHandshaker(app, host));
		helloProvider = eastWindClient.getProvider(app, HelloProvider.class);

		eastWindServer = new EastWindServer(app);
		eastWindServer.setPort(host.getPort());
		eastWindServer.setParentThreads(1);
		eastWindServer.setServerHandshaker(new ClusterServerHandshaker(uuid, eastWindClient.getChannelGuard()));
		eastWindServer.registerProvider(new HelloProviderImpl());
		eastWindServer.start();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<Host> getHosts() {
		return hosts;
	}

	public void setHosts(List<Host> hosts) {
		this.host = hosts.get(id);
		this.hosts = Lists.newLinkedList(hosts);
		this.hosts.remove(id);
	}

	public HelloProvider getHelloProvider() {
		return helloProvider;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		List<Host> hosts = Lists.newArrayList(new Host("127.0.0.1", 12468), new Host("127.0.0.1", 22468));
		TestCluster0 tc = new TestCluster0(0);
		tc.setHosts(hosts);
		tc.start();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (br.readLine() != null) {
			InvocationBuilder.builder().broadcast()
					.listen(tc.getHelloProvider().hello("baby"), new InvocationListener<String>() {
						@Override
						public void operationComplete(String result, Throwable th) {
							System.out.println(result);
						}
					});
		}
		System.in.read();
	}
}
