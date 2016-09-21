package eastwind.io3;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;

import eastwind.io3.transport.ServerRepository;

public class InvokerFactory {

	private ServerConfigurer serverConfigurer;
	private ServerRepository serverRepository;

	private ConcurrentMap<Class<?>, GroupInvoker> groupInvokers = Maps.newConcurrentMap();

	public InvokerFactory(ServerConfigurer serverConfigurer, ServerRepository serverRepository) {
		this.serverConfigurer = serverConfigurer;
		this.serverRepository = serverRepository;
	}

	@SuppressWarnings("unchecked")
	public <T> T getInvoker(String group, Class<T> interf) {
		GroupInvoker g = getGroup(group);
		synchronized (g) {
			T t = (T) g.invokers.get(interf);
			if (t == null) {
				t = (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[] { interf },
						new RemoteInvocationHandler(group, serverConfigurer, serverRepository));
				g.invokers.put(interf, t);
			}
			return t;
		}
	}

	private GroupInvoker getGroup(String group) {
		GroupInvoker g = groupInvokers.get(group);
		return g == null ? new GroupInvoker(group) : g;
	}

	static class GroupInvoker {
		String group;
		Map<Class<?>, Object> invokers = Maps.newHashMap();

		public GroupInvoker(String group) {
			this.group = group;
		}

	}
}
