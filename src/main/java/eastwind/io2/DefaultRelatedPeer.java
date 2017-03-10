package eastwind.io2;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DefaultRelatedPeer extends AbstractPeer implements RelatedPeer {

	protected InetSocketAddress remoteAddress;

	protected List<AcceptedTransport> acceptedTransports = Lists.newArrayList();
	protected List<ConnectedTransport> connectedTransports = Lists.newArrayList();

	protected Map<Method, ProviderSign> methodProviderSigns = Maps.newHashMap();
	protected Map<String, ProviderSign> nameProviderSigns = Maps.newHashMap();
	
	protected DefaultRelatedPeer(String uuid, String group, String tag, String version) {
		super.uuid = uuid;
		super.group = group;
		super.tag = tag;
		super.version = version;
	}

	@Override
	public void attach(Transport transport) {
		if (transport instanceof ConnectedTransport) {
			connectedTransports.add((ConnectedTransport) transport);
		} else if (transport instanceof AcceptedTransport) {
			acceptedTransports.add((AcceptedTransport) transport);
		}
		transport.attach(this);
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	@Override
	public ConnectedTransport getConnectedTransport() {
		if (connectedTransports.size() == 0) {
			return null;
		}
		return connectedTransports.get(0);
	}
	
	@Override
	public void addProviderSign(ProviderSign des) {
		this.nameProviderSigns.put(des.getName(), des);
		if (des.getMethod() != null) {
			this.methodProviderSigns.put(des.getMethod(), des);
		}
	}

	@Override
	public ProviderSign resolveSign(Method method) {
		return methodProviderSigns.get(method);
	}

	@Override
	public ProviderSign resolveSign(String name) {
		return nameProviderSigns.get(name);
	}
}
