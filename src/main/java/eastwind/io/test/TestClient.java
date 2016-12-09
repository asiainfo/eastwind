package eastwind.io.test;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;

import eastwind.io.EastWindClient;
import eastwind.io.TypeReference;
import eastwind.io.invocation.InvocationFuture;
import eastwind.io.invocation.InvocationListenerAdapter;
import eastwind.io.invocation.InvocationUtil;
import eastwind.io.invocation.SmartInvoker;
import eastwind.io.model.Host;
import eastwind.io.transport.Node;
import eastwind.io.transport.SimpleServerLoader;

public class TestClient {

	public static String TEST_CLIENT = "test-client";

	public static void main(String[] args) throws Throwable {
		EastWindClient client = new EastWindClient(TEST_CLIENT);
		SimpleServerLoader serverLoader = new SimpleServerLoader();
		serverLoader.setNodes(TestServer.TEST_SERVER, Lists.newArrayList(new Node(new Host("127.0.0.1", 12468))));
		client.setServerLoader(serverLoader);
		client.start();

		testProxyInvoke(client);
		testSmartInvoke(client);

		testProxyInvokeAsynchronously(client);
		testSmartInvokeAsynchronously(client);
		System.in.read();
	}

	private static void testProxyInvoke(EastWindClient client) {
		FruitProvider fruitProvider = client.createProxyInvoker(TestServer.TEST_SERVER, FruitProvider.class);
		System.out.println("create apple:" + fruitProvider.create("apple"));
	}

	private static void testProxyInvokeAsynchronously(EastWindClient client) {
		FruitProvider fruitProvider = client.createProxyInvoker(TestServer.TEST_SERVER, FruitProvider.class);
		InvocationFuture<List<Fruit>> future = InvocationUtil.makeNextAsync().get(fruitProvider.queryAll());
		future.addListener(new QueryAllPrintInvocationListener());
	}

	private static void testSmartInvoke(EastWindClient client) throws Throwable {
		SmartInvoker<Integer> fruitCreator = client.createSmartInvoker(TestServer.TEST_SERVER, "fruit/create",
				int.class);
		System.out.println("create pear:" + fruitCreator.invoke("pear"));
	}

	private static void testSmartInvokeAsynchronously(EastWindClient client) throws Throwable {
		SmartInvoker<List<Fruit>> fruitQueryer = client.createSmartInvoker(TestServer.TEST_SERVER, "fruit/queryAll",
				new TypeReference<List<Fruit>>() {
				});
		InvocationFuture<List<Fruit>> future = fruitQueryer.invokeAsynchronously();
		future.addListener(new QueryAllPrintInvocationListener());
	}

	private static class QueryAllPrintInvocationListener extends InvocationListenerAdapter<List<Fruit>> {
		@Override
		public void onSuccess(InvocationFuture<List<Fruit>> future) {
			System.out.println("query all:" + JSON.toJSONString(future.getResult()));
		}
	}
}