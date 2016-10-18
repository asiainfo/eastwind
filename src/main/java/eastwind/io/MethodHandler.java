package eastwind.io;

import java.lang.reflect.Method;

public class MethodHandler {

	private String alias;
	private Object instance;
	private Method method;
	private boolean _void;
	private int paramLen;
	private Class<?>[] parameterTypes;
	
	public MethodHandler(Object instance, Method method, String parentAlias) {
		this.instance = instance;
		this.method = method;
		this.paramLen = method.getParameterTypes().length;
		_void = method.getReturnType().equals(void.class) || method.getReturnType().equals(Void.class);
		this.alias = parentAlias + "." + method.getName();
	}

	public Object invoke(Object[] param) throws Exception {
		if (paramLen == 0) {
			return method.invoke(instance);
		} else {
			return method.invoke(instance, param);
		}
	}

	public String getAlias() {
		return alias;
	}

	public Method getTargetMethod() {
		return method;
	}

	public String getName() {
		return method.getName();
	}

	public Object getInstance() {
		return instance;
	}

	public int getParamLen() {
		return paramLen;
	}

	public boolean isVoid() {
		return _void;
	}
	
    public Class<?>[] getParameterTypes() {
    	if (parameterTypes == null) {
    		parameterTypes = method.getParameterTypes();
    	}
        return parameterTypes;
    }
}