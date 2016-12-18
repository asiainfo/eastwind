package eastwind.io2;

import java.io.IOException;


public class EastWindServer extends AbstractLocalEndPoint {

	public EastWindServer(String group) {
		this(group, null, null, DEFAULT_WEIGHT);
	}
	
	public EastWindServer(String group, String tag, String version, int weight) {
		super(group, tag, version, weight);
	}
	
	@Override
	protected NettyConnectorFactory getConnectorFactory() {
		return NettyConnectorFactorys.serverConnectorFactory();
	}

	public void start() {
		super.start();
	}
	
	
	
	public static final String TEST_SERVER = "TEST_SERVER";

	public static void main(String[] args) throws IOException {
		EastWindServer server = new EastWindServer(TEST_SERVER);
		server.start();
		System.in.read();
	}
}
