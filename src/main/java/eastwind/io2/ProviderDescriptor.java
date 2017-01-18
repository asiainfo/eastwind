package eastwind.io2;

import java.lang.reflect.Method;

import com.esotericsoftware.kryo.serializers.FieldSerializer.Optional;

public class ProviderDescriptor extends NetworkTraffic {

	@Optional("method")
	private Method method;

	private String interfName;
	private String methodName;
	private String[] parameterTypes;

	private String name;

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public String getInterfName() {
		return interfName;
	}

	public void setInterfName(String interfName) {
		this.interfName = interfName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String[] getParameterTypes() {
		return parameterTypes;
	}

	public void setParameterTypes(String[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
