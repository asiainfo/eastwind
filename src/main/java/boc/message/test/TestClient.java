package boc.message.test;

import java.io.IOException;

import boc.message.common.FutureListener;
import boc.message.common.Host;
import boc.message.common.RequestFuture;
import boc.message.nioclient.CioClient;
import boc.message.nioclient.HelloInvoker;

public class TestClient {

	private String app = "test-client";

	private CioClient cioClient;

	public TestClient() {
		cioClient = new CioClient(app);
		cioClient.start();
	}

	public CioClient getCioClient() {
		return cioClient;
	}

	public static void main(String[] args) throws IOException {
		TestClient testClient = new TestClient();
		CioClient cioClient = testClient.getCioClient();
		Host host = new Host("127.0.0.1", 19999);
		HelloInvoker helloInvoker = cioClient.createCioInvoker(host);
		helloInvoker.ruok().addFuture(new FutureListener<String>() {
			@Override
			public void operationComplete(RequestFuture<String> rf) {
				System.out.println(rf.getRespone().getResult());
			}
		});
//		System.out.println(cioInvoker.ruok().sync());
		System.out.println("111111");
		System.in.read();
	}
}
