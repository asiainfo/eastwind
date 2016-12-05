package eastwind.io.invocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import eastwind.io.model.ProviderMetaData;
import eastwind.io.support.SettableFuture;
import eastwind.io.transport.ServerRepository;
import eastwind.io.transport.ServerTransport;

public class ProxyInvocationHandler extends AbstractInvocationHandler<Method> implements InvocationHandler {

	public ProxyInvocationHandler(String group, ServerRepository serverRepository) {
		super(group, serverRepository);
	}

	@Override
	public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
		InvocationPromise ip = super.invoke(method, args);
		return ip.get();
	}

	@Override
	protected boolean isSmart(Method context) {
		return true;
	}

	@Override
	protected Type getReturnType(Method context) {
		return context.getReturnType();
	}

	@Override
	protected SettableFuture<ProviderMetaData> getProviderMetaData(Method context, ServerTransport st) {
		return st.getProviderMetaData(context);
	}

	private Object returnNull(Method context) {
		Class<?> type = context.getReturnType();
		// boolean, char, byte, short, int, long, float, double
		if (type.isPrimitive()) {
			if (type == boolean.class) {
				return false;
			}

			if (type == int.class) {
				return Integer.MIN_VALUE;
			}
			if (type == long.class) {
				return Long.MIN_VALUE;
			}

			if (type == byte.class) {
				return Byte.MIN_VALUE;
			}
			if (type == short.class) {
				return Short.MIN_VALUE;
			}
			if (type == float.class) {
				return Float.MIN_VALUE;
			}
			if (type == double.class) {
				return Double.MIN_VALUE;
			}

			if (type == char.class) {
				return (char) 0xffff;
			}
		} else {
			return null;
		}
		return null;
	}

}