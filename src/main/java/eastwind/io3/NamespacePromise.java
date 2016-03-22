package eastwind.io3;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.lang3.ClassUtils;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractFuture;

public class NamespacePromise<T> extends AbstractFuture<T> {

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

	public void m(String a1, int a2) {

	}

	public static void main(String[] args) {
		Method[] methods = NamespacePromise.class.getMethods();
		System.out.println(distance(methods[1], new Class<?>[] { String.class, byte.class }));
	}

	public static int distance(Method m, Class<?>[] parameterTypes) {
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
