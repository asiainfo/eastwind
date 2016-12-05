package eastwind.io.transport;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;

import eastwind.io.model.ProviderMetaData;
import eastwind.io.support.SettableFuture;

public class Server {

	private Node node;
	private int size;
	private List<ServerTransport> transports;
	private Map<String, ServerMetaData> serverMetaDatas = new HashMap<String, Server.ServerMetaData>(2);

	public Server(Node node, int size) {
		this.node = node;
		this.size = size;
		this.transports = new ArrayList<ServerTransport>(size);
	}

	public int getSize() {
		return size;
	}

	public int getCurrentSize() {
		return transports.size();
	}

	public ServerTransport getTransport() {
		List<ServerTransport> l = this.transports;
		int s = (int) (System.currentTimeMillis() % l.size());
		ServerTransport st = getActivedTransport(l, s, TransportStatus.OK);
		return st == null ? getActivedTransport(l, s, TransportStatus.NEW) : st;
	}

	public ServerMetaData getServerMetaData(String uuid) {
		synchronized (serverMetaDatas) {
			ServerMetaData metaData = serverMetaDatas.get(uuid);
			if (metaData == null) {
				metaData = new ServerMetaData(uuid);
				serverMetaDatas.put(uuid, metaData);
			}
			return metaData;
		}
	}
	
	private ServerTransport getActivedTransport(List<ServerTransport> l, int s, int status) {
		for (int i = 0; i < l.size(); i++) {
			int j = s + i;
			if (j >= l.size()) {
				j %= l.size();
			}
			ServerTransport st = l.get(j);
			if (st.getStatus() == TransportStatus.OK || st.getStatus() == status) {
				return st;
			}
		}
		return null;
	}

	public void addTransport(ServerTransport transport) {
		this.transports.add(transport);
	}

	static class ServerMetaData implements ProviderMetaDataVisitor {
		String uuid;
		ConcurrentMap<Method, SettableFuture<ProviderMetaData>> methodMetaDatas = Maps.newConcurrentMap();
		ConcurrentMap<String, SettableFuture<ProviderMetaData>> namedMetaDatas = Maps.newConcurrentMap();

		public ServerMetaData(String uuid) {
			this.uuid = uuid;
		}

		public String getUuid() {
			return uuid;
		}

		@Override
		public SettableFuture<ProviderMetaData> get(Method method) {
			return methodMetaDatas.get(method);
		}

		@Override
		public SettableFuture<ProviderMetaData> putIfAbsent(Method method, SettableFuture<ProviderMetaData> future) {
			return methodMetaDatas.putIfAbsent(method, future);
		}

		@Override
		public SettableFuture<ProviderMetaData> get(String name) {
			return namedMetaDatas.get(name);
		}

		@Override
		public SettableFuture<ProviderMetaData> putIfAbsent(String name, SettableFuture<ProviderMetaData> future) {
			return namedMetaDatas.putIfAbsent(name, future);
		}

	}
}
