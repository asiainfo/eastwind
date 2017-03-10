package eastwind.io2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;

import com.google.common.collect.Sets;

public class AAClient extends AbstractConfigurablePeer {

	protected AAClient(String group) {
		super.uuid = UUID.randomUUID().toString();
		super.group = group;
	}

	@Override
	protected NettyConnectorFactory getConnectorFactory() {
		return NettyConnectorFactorys.clientConnectorFactory();
	}

	public void start() {
		super.start(new InetSocketAddress(12469));
	}

	public static final String TEST_CLIENT = "TEST_CLIENT";

	public static void main(String[] args) throws IOException {
		AAClient client = new AAClient(TEST_CLIENT);
		client.start();

		// final Request request = new Request();
		// request.setDataLength(2);
		// request.setData(new String[]{"abc", "123"});

		// NettyConnectorFactory fac =
		// NettyConnectorFactorys.clientConnectorFactory();
		// NettyConnector connector = fac.createConnector(1, 2);
		// final ConnectedTransport transport =
		// connector.connect(AAServer.TEST_SERVER, new
		// InetSocketAddress(12468));
		// transport.addActiveListener(new Listener<Transport>() {
		// @Override
		// public void listen(Transport t) {
		// transport.post(new Shake());
		// transport.post(request);
		// }
		// });

		client.refresh(AAServer.TEST_SERVER, Sets.newHashSet((SocketAddress) new InetSocketAddress(12469)));

		System.in.read();
	}

}
