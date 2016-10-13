package eastwind.io.transport;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eastwind.io.support.CommonUtils;

public class ClientRepository {

	private ConcurrentMap<String, ClientGroup> clientGroups = Maps.newConcurrentMap();
	private ConcurrentMap<String, ClientTransport> transports = Maps.newConcurrentMap();
	
	public ClientTransport getTransport(String id) {
		return transports.get(id);
	}
	
	public void addTransport(ClientTransport ct) {
		Client client = getGroup(ct.getGroup()).getClient(ct.getUuid());
		client.transports.add(ct);
		transports.put(ct.getId(), ct);
	}

	private ClientGroup getGroup(String group) {
		ClientGroup cg = clientGroups.get(group);
		return cg == null ? CommonUtils.putIfAbsent(clientGroups, group, new ClientGroup(group)) : cg;
	}

	class DefaultClientTransportVisitor implements ClientTransportVisitor {

		private Client client;
		private ClientTransport defaultTransport;
		private boolean first = true;
		private Set<ClientTransport> used;

		public DefaultClientTransportVisitor(Client client, ClientTransport defaultTransport) {
			this.client = client;
			this.defaultTransport = defaultTransport;
		}

		@Override
		public ClientTransport next() {
			if (first) {
				first = false;
				return defaultTransport;
			}
			List<ClientTransport> transports = client.transports;
			if (transports.size() == 0) {
				return null;
			}
			if (transports.size() > 0) {
				synchronized (transports) {
					for (int i = 0; i < transports.size(); i++) {
						ClientTransport ct = transports.get(i);
						if (ct != defaultTransport && !getUsed().contains(ct)) {
							getUsed().add(ct);
							return ct;
						}
					}
				}
			}
			return null;
		}

		public Set<ClientTransport> getUsed() {
			if (used == null) {
				used = Sets.newHashSet();
			}
			return used;
		}

	}

	static class ClientGroup {
		String group;
		ConcurrentMap<String, Client> clients = Maps.newConcurrentMap();

		public ClientGroup(String group) {
			this.group = group;
		}

		public Client getClient(String uuid) {
			Client client = clients.get(uuid);
			return client == null ? CommonUtils.putIfAbsent(clients, uuid, new Client(group, uuid)) : client;
		}
	}

	static class Client {
		String uuid;
		String group;

		List<ClientTransport> transports = Lists.newLinkedList();

		public Client(String uuid, String group) {
			this.uuid = uuid;
			this.group = group;
		}

		public ClientTransport getTransport(String id) {
			synchronized (transports) {
				for (ClientTransport t : transports) {
					if (id.equals(t.getId())) {
						return t;
					}
				}
				return null;
			}
		}
	}

}
