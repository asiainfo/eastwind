package eastwind.io3.test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;

import eastwind.io.common.Host;
import eastwind.io3.EastwindFramework;
import eastwind.io3.MessageListener;
import eastwind.io3.Transport;
import eastwind.io3.TransportableApplicationGroup;

public class TestClient {

	public static void main(String[] args) throws IOException, InterruptedException {
		EastwindFramework ef = new EastwindFramework("test-client", false);
		ef.start();
		TransportableApplicationGroup group = ef.setApplicationConfig("test-server", Lists.newArrayList(new Host("127.0.0.1", 12468)));
		group.active();
		ef.registerMessageListener(new MessageListener<String>() {
			@Override
			public void onMessage(String message, Transport transport) {
				System.out.println(message);
			}
		});
		TimeUnit.SECONDS.sleep(1);
		group.next().send("hello");
		System.in.read();
	}

}
