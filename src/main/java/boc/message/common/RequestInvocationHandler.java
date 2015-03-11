package boc.message.common;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import boc.message.server.Provider;

public class RequestInvocationHandler implements InvocationHandler {

	private TimedIdSequence100 timedIdSequence100 = new TimedIdSequence100();
	
	private SubmitRequest submitRequest;
	
	public RequestInvocationHandler(SubmitRequest submitRequest) {
		this.submitRequest = submitRequest;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (Object.class.equals(method.getDeclaringClass())) {
			return method.invoke(this, args);
		}
		Request request = new Request();
		Provider p = method.getAnnotation(Provider.class);
		request.setId(timedIdSequence100.newId());
		if (p.value().equals("")) {
			request.setType(method.getName());
		} else {
			request.setType(p.value());
		}
		request.setArgs(args);

		RequestFuture<?> requestFuture = RequestFuture.REQUEST_FUTURE_LOCAL.get();
		requestFuture.setRequest(request);

		this.submitRequest.submit(requestFuture);

		return returnVal(method.getReturnType());
	}

	private Object returnVal(Class<?> c) {
		if (c.isPrimitive()) {
			// boolean, byte, char, short, int, long, float, and double
			return 0;
		} else {
			return null;
		}
	}
}
