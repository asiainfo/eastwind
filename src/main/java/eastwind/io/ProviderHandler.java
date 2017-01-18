package eastwind.io;

import java.lang.reflect.Method;

import eastwind.io.support.InnerUtils;
import eastwind.io2.ProviderDescriptor;

public class ProviderHandler {

	private String name;
	private Object instance;
	private Method method;
	private boolean _void;
	private int paramLen;
	private Class<?>[] parameterTypes;
	
	private ProviderDescriptor descriptor;
	
	public ProviderHandler(Object instance, Method method, String namespace) {
		this.instance = instance;
		this.method = method;
		this.paramLen = method.getParameterTypes().length;
		_void = method.getReturnType().equals(void.class) || method.getReturnType().equals(Void.class);
		this.name = InnerUtils.getFullProviderName(namespace, method.getName());
		
		this.descriptor = new ProviderDescriptor();
		descriptor.setMethod(method);
		descriptor.setInterfName(method.getDeclaringClass().getSimpleName());
		descriptor.setMethodName(method.getName());
		descriptor.setName(name);
		descriptor.setParameterTypes(InnerUtils.getParameterTypes(method));
	}

	public Object invoke(Object[] params) throws Exception {
		if (paramLen == 0) {
			return method.invoke(instance);
		} else {
			return method.invoke(instance, params);
		}
	}

	public String getName() {
		return name;
	}

	public Method getMethod() {
		return method;
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

	public ProviderDescriptor getDescriptor() {
		return descriptor;
	}

	public String stringOfParameterTypes() {
    	Class<?>[] pts = getParameterTypes();
    	if (pts.length == 0) {
    		return "--";
    	}
    	StringBuilder sb = new StringBuilder();
    	for (Class<?> cls : pts) {
    		sb.append(cls.getName()).append(",");
    	}
    	return sb.length() == 0 ? "" : sb.substring(0, sb.length() - 1);
    }
    
    public String stringOfReturnType() {
    	if (_void) {
    		return "--";
    	}
    	return method.getReturnType().getName();
    }
}
