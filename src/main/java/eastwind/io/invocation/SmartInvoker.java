package eastwind.io.invocation;

import java.lang.reflect.Type;

public class SmartInvoker<T> {

	private String group;
	private String name;
	private Type returnType;
	
	private SmartInvocationHandler invocationHandler;
	
	public SmartInvoker(String group, String name, Type returnType, SmartInvocationHandler invocationHandler) {
		this.group = group;
		this.name = name;
		this.returnType = returnType;
		this.invocationHandler = invocationHandler;
	}

	public T invoke(Object... args) throws Throwable {
		InvocationPromise ip = invocationHandler.invoke(name, args);
		return (T) ip.get();
	}
	
	public InvocationFuture<T> invokeAsynchronously(Object... args) throws Throwable {
		InvocationPromise ip = invocationHandler.invoke(name, args);
		return ip;
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
