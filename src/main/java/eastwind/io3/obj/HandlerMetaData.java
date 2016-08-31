package eastwind.io3.obj;

import java.lang.reflect.Method;

public class HandlerMetaData {

	private Method method;
	private String name;

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
