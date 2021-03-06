package eastwind.io;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eastwind.io.support.InnerUtils;

public class ProviderRegistry {

	private static Logger logger = LoggerFactory.getLogger(ProviderRegistry.class);
	
	private static List<Class<?>> primitiveWrappers = Lists.newArrayList();
	private static List<Class<?>> primitives = Lists.newArrayList();

	static {
		primitiveWrappers.add(Byte.class);
		primitiveWrappers.add(Short.class);
		primitiveWrappers.add(Integer.class);
		primitiveWrappers.add(Long.class);
		primitiveWrappers.add(Float.class);
		primitiveWrappers.add(Double.class);

		primitives.add(byte.class);
		primitives.add(short.class);
		primitives.add(int.class);
		primitives.add(long.class);
		primitives.add(float.class);
		primitives.add(double.class);
	}

	private Map<String, List<ProviderHandler>> methodHandlers = Maps.newHashMap();

	private List<ProviderInstance> providerInstances = Lists.newArrayList();

	public List<ProviderInstance> getProviderInstances() {
		return providerInstances;
	}

	public ProviderHandler findHandler(String name) {
		return methodHandlers.get(name).get(0);
	}

	public ProviderHandler findHandler(String interf, String method, String[] parameterTypes)
			throws ClassNotFoundException {
		Class<?> cls = Class.forName(interf);
		Class<?>[] pts = new Class<?>[parameterTypes.length];
		for (int i = 0; i < pts.length; i++) {
			pts[i] = Class.forName(parameterTypes[i]);
		}
	
		ProviderHandler handler = null;
		for (ProviderInstance ho : providerInstances) {
			if (ho.getInterfs().contains(cls)) {
				int distance = 0;
				for (ProviderHandler h : ho.getHandlers()) {
					int d = distance(h.getMethod(), pts);
					if (d == 0) {
						handler = h;
						break;
					}
					if (d != -1 && d < distance) {
						handler = h;
						distance = d;
					}
				}
				break;
			}
		}
	
		return handler;
	}

	public void registerProvider(Object provider) {
		List<Class<?>> interfs = Lists.newArrayList();
		addGenericInterfaces(interfs, provider.getClass());
		for (int i = 0; i < interfs.size(); i++) {
			addGenericInterfaces(interfs, interfs.get(i));
		}

		ProviderInstance pi = new ProviderInstance(newNamespace(provider), provider, interfs);

		for (Method method : provider.getClass().getMethods()) {
			if (!Object.class.equals(method.getDeclaringClass())) {
				ProviderHandler mh = new ProviderHandler(provider, method, pi.getNamespace());
				String key = InnerUtils.getFullProviderName(pi.getNamespace(), method.getName());
				List<ProviderHandler> handlers = methodHandlers.get(key);
				if (handlers == null) {
					handlers = Lists.newArrayList();
					methodHandlers.put(key, handlers);
				}
				handlers.add(mh);
				pi.getHandlers().add(mh);
				logger.info("register {}:{}", provider.getClass().getName(), mh.getName());
			}
		}
		
		providerInstances.add(pi);
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

	private String newNamespace(Object obj) {
		String name = InnerUtils.getInstanceName(obj.getClass());
		if (!duplicateOfNamespace(name)) {
			return name;
		}

//		String fullName = obj.getClass().getCanonicalName();
//		name = fullName.substring(0, fullName.lastIndexOf(".")) + "." + name;
//		for (int i = name.length(); (i = name.lastIndexOf(".", i - 1)) != -1;) {
//			String alias = name.substring(i + 1);
//			if (duplicateOfAlias(alias)) {
//				continue;
//			}
//			return alias;
//		}
		return name;
	}

	private boolean duplicateOfNamespace(String namespace) {
		for (ProviderInstance hi : providerInstances) {
			if (hi.getNamespace().equals(namespace)) {
				return true;
			}
		}
		return false;
	}

	private int distance(Method m, Class<?>[] parameterTypes) {
		Class<?>[] pts = m.getParameterTypes();
		if (pts.length != parameterTypes.length) {
			return -1;
		}
		int distance = 0;
		for (int i = 0; i < pts.length; i++) {
			Class<?> c1 = pts[i];
			Class<?> c2 = parameterTypes[i];
			if (c1 == c2) {
				continue;
			}
			if (c1.isAssignableFrom(c2)) {
				distance++;
				continue;
			}
			if (ClassUtils.isPrimitiveOrWrapper(c1) && ClassUtils.isPrimitiveOrWrapper(c2)) {
				if (ClassUtils.isPrimitiveWrapper(c1) && ClassUtils.isPrimitiveWrapper(c2)) {
					return -1;
				}
				if (c1 == (c2.isPrimitive() ? ClassUtils.primitiveToWrapper(c2) : ClassUtils.wrapperToPrimitive(c2))) {
					continue;
				}
				if (c1.isPrimitive()) {
					int i1 = primitives.indexOf(c1);
					int i2 = c2.isPrimitive() ? primitives.indexOf(c2) : primitiveWrappers.indexOf(c2);
					if (i1 > i2) {
						distance++;
						continue;
					}
				}
			}
			return -1;
		}
		return distance;
	}

}
