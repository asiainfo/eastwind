package eastwind.io.model;

import java.lang.reflect.Method;

public class ProviderMetaData {

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
