package eastwind.io.invocation;

import java.lang.reflect.Type;

public class JsonInvoker<T> {

	private String group;
	private String name;
	private Type returnType;
	
	private JsonInvocationHandler invocationHandler;
	
	public JsonInvoker(String group, String name, Type returnType, JsonInvocationHandler invocationHandler) {
		this.group = group;
		this.name = name;
		this.returnType = returnType;
		this.invocationHandler = invocationHandler;
	}

	@SuppressWarnings("unchecked")
	public T invoke(Object... args) throws Throwable {
		return (T) invocationHandler.invoke(name, args);
	}
	
	public void asyncInvoke(Object... args) {
		
	}
	
	public String getGroup() {
		return group;
	}

	public String getName() {
		return name;
	}

	public Type getReturnType() {
		return returnType;
	}

}
