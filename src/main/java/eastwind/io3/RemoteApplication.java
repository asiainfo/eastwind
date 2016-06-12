package eastwind.io3;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eastwind.io.common.Host;

public class RemoteApplication extends Application {

	private Map<Long, HandlingMessage> handings = Maps.newHashMap();
	private List<Transport> transports;
	private List<ServerTransport> serverTransports;
	private Map<Method, ListenablePromise<String>> acceptMethods = Maps.newHashMap();
	
	public RemoteApplication(String group) {
		super(group);
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
	
	public void addMessage(HandlingMessage handlingMessage) {
		handings.put(handlingMessage.getId(), handlingMessage);
	}
	
	public void removeMessage(Long id) {
		handings.remove(id);
	}
	
	public HandlingMessage getMessage(Long id) {
		return handings.get(id);
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
