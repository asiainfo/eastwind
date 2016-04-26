package eastwind.io3;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eastwind.io.common.Host;

public class RemoteApplication extends Application {

	private List<Transport> transports;
	private List<ServerTransport> serverTransports;
	private Map<Method, ListenablePromise<String>> acceptMethods = Maps.newHashMap();
	private int i;
	
	public RemoteApplication(String group) {
		super(group);
	}

	public ServerTransport next() {
		if (i >= serverTransports.size()) {
			if (i != 0) {
				i = 0;
			}
			return null;
		}
		return serverTransports.get(i++);
	}
	
	public void setUuid(String uuid) {
		super.uuid = uuid;
	}

	public void addAcceptMethodPromise(ListenablePromise<String> lp) {
		acceptMethods.put((Method) lp.getAttach(), lp);
	}
	
	public ListenablePromise<String> acceptable(Method method) {
		return acceptMethods.get(method);
	}
	
	public void addServerTransport(ServerTransport serverTransport) {
		getServerTransports().add(serverTransport);
	}
	
	public void addTransport(Transport transport) {
		List<Transport> transports = getTransports();
		synchronized (transports) {
			for (Iterator<Transport> it = transports.iterator(); it.hasNext();) {
				Transport t = it.next();
				if (t.getState() == Transport.CONNECT_FAIL) {
					it.remove();
					continue;
				}
			}
			transports.add(transport);
		}
	}

	public ServerTransport getTransport(Host host) {
		if (serverTransports == null) {
			return null;
		}
		for (ServerTransport st : getServerTransports()) {
			if (st.getServerConfig().getHost().equals(host)) {
				return st;
			}
		}
		return null;
	}
	
	private List<ServerTransport> getServerTransports() {
		if (serverTransports == null) {
			serverTransports = Lists.newArrayList();
		}
		return serverTransports;
	}

	private List<Transport> getTransports() {
		if (transports == null) {
			transports = Lists.newArrayList();
		}
		return transports;
	}
}
