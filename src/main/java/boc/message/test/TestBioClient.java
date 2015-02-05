package boc.message.test;

import java.io.IOException;
import java.net.SocketAddress;

import boc.message.bioclient.CioClient;
import boc.message.bioclient.NetState;
import boc.message.bioclient.NetStateListener;

public class TestBioClient {

	public static void main(String[] args) throws IOException {
		final CioClient cioClient = new CioClient("test-bio-client").start();
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
				System.out.println(cioClient.getCioInvoker().ruok().sync());
			}
		});
		System.in.read();
	}

}
