package eastwind.io.server;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ProviderManager {

	private static AtomicInteger INTERF_ID = new AtomicInteger(1);
	private Map<String, String> interfIds = Maps.newHashMap();
	private Map<String, Map<String, ProviderHandler>> idProviders = Maps.newHashMap();
	private Map<String, Map<String, ProviderHandler>> nameProviders = Maps.newHashMap();

	public synchronized void register(Object provider) {
		List<Class<?>> interfs = Lists.newArrayList();
		addGenericInterfaces(interfs, provider.getClass());
		for (int i = 0; i < interfs.size(); i++) {
			addGenericInterfaces(interfs, interfs.get(i));
		}

		Set<Method> methods = Sets.newHashSet();
		for (int i = interfs.size() - 1; i >= 0; i--) {
			Class<?> c = interfs.get(i);
			for (Method m : c.getMethods()) {
				methods.add(m);
			}
		}

		for (Method m : methods) {
			ProviderHandler handler = new ProviderHandler(provider, m);
			String className = m.getDeclaringClass().getCanonicalName();
			Map<String, ProviderHandler> map = nameProviders.get(className);
			if (map == null) {
				map = Maps.newHashMap();
				nameProviders.put(className, map);
				String classId = interfIds.get(className);
				if (classId == null) {
					classId = String.valueOf(INTERF_ID.getAndIncrement());
					interfIds.put(className, classId);
				}
				idProviders.put(classId.toString(), map);
			}
			map.put(handler.getName(), handler);
		}
	}

	public synchronized String getInterfId(String interf) {
		return interfIds.get(interf);
	}

	public ProviderHandler get(String interf, String name) {
		if (StringUtils.isNumeric(interf)) {
			return idProviders.get(interf).get(name);
		}
		return nameProviders.get(interf).get(name);
	}

	private void addGenericInterfaces(List<Class<?>> interfs, Class<?> c) {
		for (Type t : c.getGenericInterfaces()) {
			if (Class.class.isInstance(t)) {
				interfs.add((Class<?>) t);
			} else if (ParameterizedType.class.isInstance(t)) {
				Type tp = ((ParameterizedType) t).getRawType();
				if (Class.class.isInstance(tp)) {
					interfs.add((Class<?>) tp);
				}
			}
		}
	}
}
