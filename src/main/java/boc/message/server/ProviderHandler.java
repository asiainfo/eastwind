package boc.message.server;

import java.lang.reflect.Method;

public class ProviderHandler {

	private String name;
	private Object instance;
	private Method method;
	private boolean _void;
	private int paramNum;

	public ProviderHandler(Object instance, String name, Method method) {
		this.instance = instance;
		this.name = name;
		this.method = method;
		this.paramNum = method.getParameterTypes().length;
		_void = method.getReturnType().equals(void.class) || method.getReturnType().equals(Void.class);
	}

	public Object invoke(Object param) throws Exception {
		if (paramNum == 0) {
			return method.invoke(instance);
		} else {
			return method.invoke(instance, new Object[] { param });
		}
	}

	public int getParamNum() {
		return paramNum;
	}

	public Method getTargetMethod() {
		return method;
	}

	public String getName() {
		return name;
	}

	public Object getInstance() {
		return instance;
	}

	public boolean isVoid() {
		return _void;
	}

}