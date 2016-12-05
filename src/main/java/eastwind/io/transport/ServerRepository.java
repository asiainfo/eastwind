package eastwind.io.transport;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;

import eastwind.io.support.OperationListener;

public class ServerRepository {

	private int connectionsPerHost;
	private TransportFactory transportFactory;
	private Map<String, ServerGroup> groups = Maps.newHashMap();
	private ConcurrentMap<String, ServerTransport> transports = Maps.newConcurrentMap();

	private ServerLoader serverLoader;

	public ServerRepository(TransportFactory transportFactory, int connectionsPerHost) {
		this.transportFactory = transportFactory;
		this.connectionsPerHost = connectionsPerHost;
	}

	public ServerTransport getTransport(String id) {
		return transports.get(id);
	}

	public ServerTransport getTransport(String group, Node node) {
		tryNewTransport(group, node);
		Server server = getServerGroup(group).getServer(node);
		ServerTransport st = server.getTransport();
		if (st != null) {
			return st;
		}
		// TODO clean
		if (tryNewTransport(group, node)) {
			return server.getTransport();
		}
		return null;
	}

	public void setServerLoader(ServerLoader serverLoader) {
		this.serverLoader = serverLoader;
	}

	private boolean tryNewTransport(String group, Node node) {
		final Server server = getServerGroup(group).getServer(node);
		if (server.getSize() != server.getCurrentSize()) {
			final ServerTransport st = transportFactory.serverTransport(group, node);
			server.addTransport(st);
			transports.put(st.getId(), st);
			st.addShakeListener(new Shake2SetProviderMetaDataListener(server, st));
			return true;
		}
		return false;
	}

	public ServerGroup getServerGroup(String group) {
		synchronized (groups) {
			ServerGroup sg = groups.get(group);
			if (sg == null) {
				sg = new ServerGroup(group, connectionsPerHost);
				sg.initNodes(serverLoader.getNodes(group));
				groups.put(group, sg);
			}
			return sg;
		}
	}

	private static final class Shake2SetProviderMetaDataListener implements OperationListener<ServerTransport> {
		private final Server server;
		private final ServerTransport st;

		private Shake2SetProviderMetaDataListener(Server server, ServerTransport st) {
			this.server = server;
			this.st = st;
		}

		@Override
		public void complete(ServerTransport t) {
			if (t.getStatus() == TransportStatus.OK) {
				st.setProviderMetaDataVisitor(server.getServerMetaData(st.getUuid()));
			}
		}
	}
}