package eastwind.io.test;

import java.io.IOException;
import java.net.SocketAddress;

import eastwind.io.bioclient.EastWindClient;
import eastwind.io.bioclient.NetState;
import eastwind.io.bioclient.NetStateListener;
import eastwind.io.common.Host;

public class TestBioClient {

	public static void main(String[] args) throws IOException {
		final EastWindClient cioClient = new EastWindClient("test-bio-client").start();
		Host host = new Host("127.0.0.1", 12468);
		cioClient.connect(host);
		cioClient.addNetStateListener(new NetStateListener() {
			@Override
			public void stateChanged(SocketAddress socketAddress, NetState netState) {
				System.out.println(netState);
			}
		});
		System.in.read();
	}

}
