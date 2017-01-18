package eastwind.io2;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class RemoteEndPoint extends AbstractEndPoint implements NetworkTrafficEndPoint {

	protected LocalEndPoint localEndPoint;
	protected InetSocketAddress remoteAddress;

	protected List<InboundTransport> inboundTransports = Lists.newArrayList();
	protected List<OutboundTransport> outboundTransports = Lists.newArrayList();

	protected Map<Method, ProviderDescriptor> methodMetaDatas = Maps.newHashMap();
	protected Map<String, ProviderDescriptor> namedMetaDatas = Maps.newHashMap();
	
	protected RemoteEndPoint(String uuid, String group, String tag, String version) {
		super(uuid, group, tag, version);
	}

	@Override
	public void attach(Transport transport) {
		if (transport instanceof OutboundTransport) {
			outboundTransports.add((OutboundTransport) transport);
		} else if (transport instanceof InboundTransport) {
			inboundTransports.add((InboundTransport) transport);
		}
		transport.attach(this);
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	@Override
	public boolean invokable() {
		return false;
	}

	@Override
	public OutboundTransport getOutboundTransport() {
		if (outboundTransports.size() == 0) {
			return null;
		}
		return outboundTransports.get(0);
	}
	
	@Override
	public void addDescriptor(ProviderDescriptor des) {
		this.namedMetaDatas.put(des.getName(), des);
		if (des.getMethod() != null) {
			this.methodMetaDatas.put(des.getMethod(), des);
		}
	}

	@Override
	public ProviderDescriptor getDescriptor(Method method) {
		return methodMetaDatas.get(method);
	}

	@Override
	public ProviderDescriptor getDescriptor(String name) {
		return namedMetaDatas.get(name);
	}
}
