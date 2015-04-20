package eastwind.io.test;

import java.io.IOException;
import java.net.SocketAddress;

import eastwind.io.bioclient.EastWindClient;
import eastwind.io.bioclient.NetState;
import eastwind.io.bioclient.NetStateListener;

public class TestBioClient {

	public static void main(String[] args) throws IOException {
		final EastWindClient cioClient = new EastWindClient("test-bio-client").start();
		cioClient.connect("127.0.0.1", 19999);
		cioClient.addNetStateListener(new NetStateListener() {
			@Override
			public boolean oneOff() {
				return true;
			}

			@Override
			public NetState ExecuteIfOnState() {
				return NetState.CONNECTED;
			}

			@Override
			public void stateChanged(SocketAddress socketAddress, NetState netState) {
				System.out.println(netState);
			}
		});
		System.in.read();
	}

}
