package eastwind.io.transport;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.google.common.collect.Lists;

import eastwind.io.invocation.Route;
import eastwind.io.invocation.InvocationInfo;
import eastwind.io.support.ExpressionUtil;

public class RouteGroup {

	private List<Route> routes = Lists.newArrayList();
	
	public List<Route> getRoutes() {
		return routes;
	}

	public void add(Route route) {
		if (routes.size() == 0) {
			routes.add(route);
			return;
		}
		for (ListIterator<Route> it = routes.listIterator();;) {
			Route r = it.next();
			if (r.getPrecedence() > route.getPrecedence()
					|| (r.getPrecedence() == route.getPrecedence() && r.getTime().getTime() < route.getTime()
							.getTime())) {
				it.previous();
				it.add(route);
				break;
			}
			if (it.hasNext()) {
				continue;
			} else {
				it.add(route);
				break;
			}
		}
	}
	
	public Route next(Route previous, InvocationInfo info) {
		if (previous == null) {
			return routes.size() == 0 ? null : routes.get(0);
		}
		for (Route r : routes) {
			if (r.getPrecedence() > previous.getPrecedence()
					|| (r != previous && r.getTime().getTime() > previous.getTime().getTime())) {
				if (match(r, info)) {
					return r;
				}
			}
		}
		return null;
	}

	public void remove(Long id) {
		Iterator<Route> it = routes.iterator();
		while (it.hasNext()) {
			if (it.next().getId() == id.longValue()) {
				it.remove();
				return;
			}
		}
	}
	
	private boolean match(Route route, InvocationInfo info) {
		if (route.getConsumer() != null) {
			if (!info.getGroup().equals(route.getConsumer())) {
				return false;
			}
			String fromVersion = route.getFromVersion();
			if (fromVersion != null && !fromVersion.equals(info.getVersion())) {
				return false;
			}
		}

//		List<String> ips = route.getFromIps();
//		if (ips != null && ips.size() > 0) {
//			if (!ips.contains(info.getIp())) {
//				return false;
//			}
//		}

		String expression = route.getExpression();
		if (expression != null) {
			return ExpressionUtil.execute(expression, info.getArgs()).equals("false");
		}
		return true;
	}
}
