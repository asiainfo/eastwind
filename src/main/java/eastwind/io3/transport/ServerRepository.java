package eastwind.io3.transport;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eastwind.io3.model.HandlerMetaData;
import eastwind.io3.model.Host;
import eastwind.io3.support.CommonUtils;
import eastwind.io3.support.OperationListener;
import eastwind.io3.support.SettableFuture;

public class ServerRepository {

	private ConcurrentMap<String, ServerGroup> serverGroups = Maps.newConcurrentMap();
	private ConcurrentMap<String, ServerTransport> transports = Maps.newConcurrentMap();
	private TransportFactory transportFactory;

	public ServerRepository(TransportFactory transportFactory) {
		this.transportFactory = transportFactory;
	}

	public ServerTransport getTransport(String id) {
		return transports.get(id);
	}

	public ServerTransport getTransport(String group, Host host) {
		return findOrCreate(group, host);
	}

	public ServerTransportVisitor getTransportVisitor(String group, boolean oneOff) {
		return new DefaultTransportVisitor(group, oneOff);
	}

	private ServerGroup getGroup(String group) {
		ServerGroup sg = serverGroups.get(group);
		return sg == null ? CommonUtils.putIfAbsent(serverGroups, group, new ServerGroup(group)) : sg;
	}

	private ServerTransport findOrCreate(String group, Host host) {
		final Server server = getGroup(group).getServer(host);
		ServerTransport st = find(server, host);
		if (st == null) {
			st = transportFactory.serverTransport(group, host);
			server.transports.add(st);
			transports.put(st.getId(), st);

			st.addShakeListener(new OperationListener<ServerTransport>() {
				@Override
				public void complete(ServerTransport st) {
					if (st.getStatus() == 1) {
						ServerHandler sh = server.serverHandlers.get(st.getUuid());
						if (sh == null) {
							sh = CommonUtils.putIfAbsent(server.serverHandlers, st.getUuid(),
									new ServerHandler(st.getUuid()));
						}
						st.setServerHandlerMetaData(sh.serverHandlerMetaData);
					}
				}
			});
		}
		return st;
	}

	private ServerTransport find(Server server, Host host) {
		for (ServerTransport st : server.transports) {
			if (st.getHost() == host) {
				return st;
			}
		}
		return null;
	}

	static class ServerGroup {
		String group;
		ConcurrentMap<Host, Server> servers = Maps.newConcurrentMap();

		public ServerGroup(String group) {
			this.group = group;
		}

		public Server getServer(Host host) {
			Server server = servers.get(host);
			return server == null ? CommonUtils.putIfAbsent(servers, host, new Server(host)) : server;
		}
	}

	static class Server {
		Host host;
		List<ServerTransport> transports = Lists.newLinkedList();
		ConcurrentMap<String, ServerHandler> serverHandlers = Maps.newConcurrentMap();

		public Server(Host host) {
			this.host = host;
		}
	}

	static class ServerHandler {
		String uuid;
		ServerHandlerMetaData serverHandlerMetaData = new ServerHandlerMetaData();

		public ServerHandler(String uuid) {
			this.uuid = uuid;
		}
	}

	static class ServerHandlerMetaData {
		ConcurrentMap<Method, SettableFuture<HandlerMetaData>> methodMetaDatas = Maps.newConcurrentMap();
		ConcurrentMap<String, SettableFuture<HandlerMetaData>> namedMetaDatas = Maps.newConcurrentMap();

		public SettableFuture<HandlerMetaData> get(Method method) {
			return methodMetaDatas.get(method);
		}

		public SettableFuture<HandlerMetaData> putIfAbsent(Method method, SettableFuture<HandlerMetaData> future) {
			return methodMetaDatas.putIfAbsent(method, future);
		}
		
		public SettableFuture<HandlerMetaData> get(String name) {
			return namedMetaDatas.get(name);
		}

		public SettableFuture<HandlerMetaData> putIfAbsent(String name, SettableFuture<HandlerMetaData> future) {
			return namedMetaDatas.putIfAbsent(name, future);
		}
	}

	class DefaultTransportVisitor implements ServerTransportVisitor {

		private boolean oneOff;
		private Set<ServerTransport> used;
		private String group;

		public DefaultTransportVisitor(String group, boolean oneOff) {
			this.group = group;
			this.oneOff = oneOff;
		}

		private Set<ServerTransport> getUsed() {
			if (used == null) {
				used = Sets.newHashSet();
			}
			return used;
		}

		@Override
		public boolean oneOff() {
			return oneOff;
		}

		@Override
		public ServerTransport next(Host host) {
			return findOrCreate(group, host);
		}
	}
}
