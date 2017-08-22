package eastwind.io3.support;

import java.util.List;

public class SupportUtil {

	public static String getName(Object instance) {
		if (instance == null) {
			return null;
		}
		Name name = instance.getClass().getAnnotation(Name.class);
		if (name == null) {
			String simpleName = instance.getClass().getSimpleName();
			if (simpleName.length() == 1) {
				return simpleName.substring(0, 1).toLowerCase();
			}
			return simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
		} else {
			return name.value();
		}
	}

	public static <T extends Ordered> void add(List<T> list, T ordered) {
		int i = 0;
		for (; i < list.size(); i++) {
			if (list.get(i).getOrder() > ordered.getOrder()) {
				break;
			}
		}
		list.add(i, ordered);
	}
}
