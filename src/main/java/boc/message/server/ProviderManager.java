package boc.message.server;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ProviderManager {

	private Map<String, ProviderHandler> providers = Maps.newConcurrentMap();

	public void register(Object provider) {
		Class<?> c = provider.getClass();
		Type[] types = c.getGenericInterfaces();
		List<Method> superHandlers = Lists.newArrayList();
		for (Type t : types) {
			if (Class.class.isInstance(t)) {
				Class<?> cls = (Class<?>) t;
				for (Method m : cls.getMethods()) {
					if (m.getAnnotation(Provider.class) != null) {
						superHandlers.add(m);
					}
				}
			}
		}

		Map<String, Method> map = Maps.newHashMap();
		for (Method m : superHandlers) {
			Provider p = m.getAnnotation(Provider.class);
			String key = p.value();
			if (StringUtils.isBlank(key)) {
				key = m.getName();
			}
			map.put(key.trim(), m);
		}

		for (Entry<String, Method> en : map.entrySet()) {
			ProviderHandler handler = new ProviderHandler(provider, en.getKey(), en.getValue());
			providers.put(en.getKey(), handler);
		}
	}

	public ProviderHandler get(String name) {
		return providers.get(name);
	}
}
