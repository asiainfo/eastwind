package eastwind.io2;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.alibaba.fastjson.JSON;

import eastwind.io.support.InnerUtils;

public class StubInvocationHandler implements InvocationHandler {

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Request request = new Request();
		request.setDataLength(args.length);
		if (args.length > 0) {
			request.setData(args.length == 1 ? args[0] : args);
		}
		String ns = InnerUtils.getInstanceName(method.getDeclaringClass());
		String name = InnerUtils.getMethodName(method);
		request.setName(InnerUtils.getFullProviderName(ns, name));
		Constants.REQUEST.set(request);
		return InnerUtils.returnNull(method);
	}

	public static void main(String[] args) {
		InternalProvider p = (InternalProvider) Proxy.newProxyInstance(StubInvocationHandler.class.getClassLoader(), new Class<?>[] { InternalProvider.class },
				new StubInvocationHandler());
		p.desc(new ProviderDescriptor());
		System.out.println(JSON.toJSONString(InnerUtils.getThreadLocal(Constants.REQUEST)));
	}
}
