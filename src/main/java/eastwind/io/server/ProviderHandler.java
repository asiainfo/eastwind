package eastwind.io.server;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

public class ProviderHandler {

	private static AtomicInteger NEXT_ID = new AtomicInteger(1);
	
	private int id = NEXT_ID.getAndIncrement();
	private Object instance;
	private Method method;
	private boolean _void;
	private int paramNum;

	public ProviderHandler(Object instance, Method method) {
		this.instance = instance;
		this.method = method;
		this.paramNum = method.getParameterTypes().length;
		_void = method.getReturnType().equals(void.class) || method.getReturnType().equals(Void.class);
	}

	public Object invoke(Object[] param) throws Exception {
		if (paramNum == 0) {
			return method.invoke(instance);
		} else {
			return method.invoke(instance, param);
		}
	}

	public int getId() {
		return id;
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

	public boolean isVoid() {
		return _void;
	}

}