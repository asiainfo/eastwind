package eastwind.io2.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;

import eastwind.io2.AbstractConfigurablePeer;
import eastwind.io2.NettyConnectorFactory;
import eastwind.io2.NettyConnectorFactorys;

public class AAServer extends AbstractConfigurablePeer {

	public AAServer(String group) {
		super.uuid = UUID.randomUUID().toString();
		super.group = group;
	}

	@Override
	protected NettyConnectorFactory getConnectorFactory() {
		return NettyConnectorFactorys.serverConnectorFactory();
	}

	public void start() {
		super.start(new InetSocketAddress("127.0.0.1", 12469));
	}

	public static final String TEST_SERVER = "TEST_SERVER";

	public static void main(String[] args) throws IOException {
		AAServer server = new AAServer(TEST_SERVER);
		server.start();
		System.in.read();
	}
}
