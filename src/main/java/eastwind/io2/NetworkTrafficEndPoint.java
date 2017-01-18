package eastwind.io2;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;

public interface NetworkTrafficEndPoint extends EndPoint {

	InetSocketAddress getRemoteAddress();

	boolean invokable();

	void attach(Transport transport);

	OutboundTransport getOutboundTransport();

	void addDescriptor(ProviderDescriptor desc);

	ProviderDescriptor getDescriptor(Method method);

	ProviderDescriptor getDescriptor(String name);
}
