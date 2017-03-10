package eastwind.io2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;

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
		super.start(new InetSocketAddress(12469));
	}

	public static final String TEST_SERVER = "TEST_SERVER";

	public static void main(String[] args) throws IOException {
		AAServer server = new AAServer(TEST_SERVER);
		server.start();
		System.in.read();
	}
}
