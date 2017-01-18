package eastwind.io2;

import java.io.IOException;
import java.util.UUID;

public class EastWindServer extends AbstractAcceptorEndPoint {

	public EastWindServer(String group) {
		super(UUID.randomUUID().toString(), group, null, null);
	}

	@Override
	protected NettyConnectorFactory getConnectorFactory() {
		return NettyConnectorFactorys.serverConnectorFactory();
	}

	public void start() {
		super.init();
	}

	public static final String TEST_SERVER = "TEST_SERVER";

	public static void main(String[] args) throws IOException {
		EastWindServer server = new EastWindServer(TEST_SERVER);
		server.start();
		System.in.read();
	}
}
