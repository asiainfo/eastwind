package eastwind.io.invocation;

import java.util.Set;

import eastwind.io.transport.Node;

public class RouteServerSelector implements ServerSelector {

	private Route route;

	public RouteServerSelector(Route route) {
		this.route = route;
	}

	@Override
	public void prepare(NodeGroup nodeGroup) {
		// TODO Auto-generated method stub
	}

	@Override
	public Node next(int key, Set<Node> exculsions) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean matchName(String name) {
		if (route.getName() != null) {
			return route.getName().equals(name);
		}
		return true;
	}

	@Override
	public boolean exclusive() {
		return route.isExclusive();
	}

	@Override
	public boolean skippable() {
		return route.isSkippable();
	}

	public Route getRoute() {
		return route;
	}

	@Override
	public HashCodeGenerator getHashCodeGenerator() {
		return RandomHashCodeGenerator.DEFAULT;
	}

}
