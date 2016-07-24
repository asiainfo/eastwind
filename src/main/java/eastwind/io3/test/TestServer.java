package eastwind.io3.test;

import java.io.IOException;

import eastwind.io.test.HelloImpl;
import eastwind.io3.EastwindFramework;

public class TestServer {

	public static void main(String[] args) throws IOException {
		EastwindFramework ef = new EastwindFramework("test-server");
		ef.registerHandler(new HelloImpl());
//		ef.registerListener(new MessageListener<String>() {
//			@Override
//			public void onMessage(String message, Transport transport) {
//				System.out.println(message);
//				transport.publish(message);
//			}
//		});
		System.in.read();
	}
	
}
