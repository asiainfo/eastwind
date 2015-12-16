package eastwind.io2;

import java.lang.reflect.Method;

public class EastwindUtils {

	public static String getDefaultNamespace(Method method) {
		String className = method.getDeclaringClass().getSimpleName();
		String _className = className.substring(0, 1).toLowerCase();
		if (className.length() > 1) {
			_className += className.substring(1);
		}
		return _className + "." + method.getName();
	}

}
