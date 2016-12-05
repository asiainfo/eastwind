package eastwind.io;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;

import eastwind.io.invocation.ProxyInvocationHandler;
import eastwind.io.transport.ServerRepository;

public class InvokerFactory {

	private ServerRepository serverRepository;

	private ConcurrentMap<Class<?>, InvokerGroup> groupInvokers = Maps.newConcurrentMap();

	public InvokerFactory(ServerRepository serverRepository) {
		this.serverRepository = serverRepository;
	}

	@SuppressWarnings("unchecked")
	public <T> T getInvoker(String group, Class<T> interf) {
		InvokerGroup g = getGroup(group);
		synchronized (g) {
			T t = (T) g.invokers.get(interf);
			if (t == null) {
				t = (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[] { interf },
						new ProxyInvocationHandler(group, serverRepository));
				g.invokers.put(interf, t);
			}
			return t;
		}
	}

	private InvokerGroup getGroup(String group) {
		InvokerGroup g = groupInvokers.get(group);
		return g == null ? new InvokerGroup(group) : g;
	}

	static class InvokerGroup {
		String group;
		Map<Class<?>, Object> invokers = Maps.newHashMap();

		public InvokerGroup(String group) {
			this.group = group;
		}

	}
}
