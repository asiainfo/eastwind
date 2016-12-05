package eastwind.io.transport;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import eastwind.io.invocation.HashServerSelector;
import eastwind.io.invocation.NodeGroup;
import eastwind.io.invocation.Route;
import eastwind.io.invocation.RouteServerSelector;
import eastwind.io.invocation.InvocationInfo;

public class ServerGroup {

	private String name;
	private int connectionsPerHost;
	private Map<Node, Server> servers = Maps.newHashMap();
	private RouteGroup routeGroup = new RouteGroup();
	private NodeGroup nodeGroup = new NodeGroup();
	private Map<Route, RouteServerSelector> selectors = Maps.newHashMap();
	private HashServerSelector defaultSelector = new HashServerSelector();
	
	public ServerGroup(String name, int connectionsPerHost) {
		this.name = name;
		this.connectionsPerHost = connectionsPerHost;
	}

	public String getName() {
		return name;
	}

	public void initNodes(List<Node> nodes) {
		nodeGroup.init(new HashSet<Node>(nodes));
		defaultSelector.prepare(nodeGroup);
	}
	
	public void add(Route route) {
		routeGroup.add(route);
	}
	
	public RouteServerSelector next(RouteServerSelector serverSelector, InvocationInfo info) {
		Route previous = serverSelector == null ? null : serverSelector.getRoute();
		Route next = routeGroup.next(previous, info);
		if (next != null) {
			RouteServerSelector selector = selectors.get(next);
			if (selector == null) {
				selector = new RouteServerSelector(next);
				selectors.put(next, selector);
			}
			return selector;
		}
		return null;
	}

	public HashServerSelector getDefaultSelector() {
		return defaultSelector;
	}

	public Server getServer(Node node) {
		synchronized (servers) {
			Server server = servers.get(node);
			if (server == null) {
				server = new Server(node, connectionsPerHost);
				servers.put(node, server);
			}
			return server;
		}
	}
	
}
