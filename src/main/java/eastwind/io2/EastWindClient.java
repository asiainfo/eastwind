package eastwind.io2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;

public class EastWindClient extends AbstractAcceptorEndPoint {

	protected EastWindClient(String group) {
		super(UUID.randomUUID().toString(), group, null, null);
	}

	@Override
	protected NettyConnectorFactory getConnectorFactory() {
		return NettyConnectorFactorys.clientConnectorFactory();
	}

	public void start() {
		setLocalAddress(new InetSocketAddress(12469));
		super.init();
	}
	
	public static final String TEST_CLIENT = "TEST_CLIENT";
	
	public static void main(String[] args) throws IOException {
//		EastWindClient client = new EastWindClient(TEST_CLIENT);
//		client.start();
		
		final Request request = new Request();
		request.setDataLength(2);
		request.setData(new String[]{"abc", "123"});
		
		NettyConnectorFactory fac = NettyConnectorFactorys.clientConnectorFactory();
		NettyConnector connector = fac.createConnector(1, 2);
		final OutboundTransport transport = connector.connect(EastWindServer.TEST_SERVER, new InetSocketAddress(12468));
		transport.addActiveListener(new Listener<Transport>() {
			@Override
			public void listen(Transport t) {
//				transport.post(new Shake());
				transport.post(request);
			}
		});
		System.in.read();
	}

}
