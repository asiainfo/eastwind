package eastwind.io3.transport;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eastwind.io3.obj.HandlerMetaData;
import eastwind.io3.obj.Host;
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
	
	public void addTransport(ServerTransport transport) {
		getGroup(transport.getGroup()).getServer(transport.getHost()).transports.add(transport);
		transports.put(transport.getId(), transport);
	}
	
	public ServerTransportVisitor getTransportVisitor(String group, boolean oneOff) {
		return new DefaultTransportVisitor(getGroup(group), oneOff);
	}

	private ServerGroup getGroup(String group) {
		ServerGroup sg = serverGroups.get(group);
		return sg == null ? CommonUtils.putIfAbsent(serverGroups, group, new ServerGroup(group)) : sg;
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
		ConcurrentMap<Method, SettableFuture<HandlerMetaData>> handlerMetaDatas = Maps.newConcurrentMap();

		public ServerHandler(String uuid) {
			this.uuid = uuid;
		}
	}

	class DefaultTransportVisitor implements ServerTransportVisitor {

		private boolean oneOff;
		private Set<ServerTransport> used;
		private ServerGroup serverGroup;

		public DefaultTransportVisitor(ServerGroup serverGroup, boolean oneOff) {
			this.serverGroup = serverGroup;
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
			final Server server = serverGroup.servers.get(host);
			ServerTransport st = findTransport(server, host);
			if (st == null) {
				st = transportFactory.serverTransport(serverGroup.group, host);
				st.addShakeListener(new OperationListener<ServerTransport>() {
					@Override
					public void complete(ServerTransport st) {
						if (st.getStatus() == 1) {
							ServerHandler sh = server.serverHandlers.get(st.getUuid());
							if (sh == null) {
								sh = CommonUtils.putIfAbsent(server.serverHandlers, st.getUuid(),
										new ServerHandler(st.getUuid()));
							}
							st.setHandlerMetaDatas(sh.handlerMetaDatas);
						}
					}
				});
				server.transports.add(st);
			}
			return st;
		}

		private ServerTransport findTransport(Server server, Host host) {
			for (ServerTransport st : server.transports) {
				if (st.getHost() == host) {
					return st;
				}
			}
			return null;
		}
	}
}
