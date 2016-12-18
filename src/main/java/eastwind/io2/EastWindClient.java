package eastwind.io2;

import java.io.IOException;
import java.net.InetSocketAddress;

public class EastWindClient extends AbstractLocalEndPoint {

	protected EastWindClient(String group) {
		super(group, null, null, DEFAULT_WEIGHT);
	}

	@Override
	protected NettyConnectorFactory getConnectorFactory() {
		return NettyConnectorFactorys.clientConnectorFactory();
	}

	public void start() {
		setLocalAddress(new InetSocketAddress(12469));
		super.start();
	}
	
	
	public static final String TEST_CLIENT = "TEST_CLIENT";
	
	public static void main(String[] args) throws IOException {
//		EastWindClient client = new EastWindClient(TEST_CLIENT);
//		client.start();
		
		NettyConnectorFactory fac = NettyConnectorFactorys.clientConnectorFactory();
		NettyConnector connector = fac.createConnector(1, 2);
		Transport transport = connector.connect(new InetSocketAddress(12468));
		
		System.in.read();
	}
}
