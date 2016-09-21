package eastwind.io3;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import eastwind.io3.obj.HandlerMetaData;
import eastwind.io3.obj.Host;
import eastwind.io3.obj.Request;
import eastwind.io3.support.GlobalExecutor;
import eastwind.io3.support.OperationListener;
import eastwind.io3.support.SettableFuture;
import eastwind.io3.transport.DefaultServerSelector;
import eastwind.io3.transport.ServerRepository;
import eastwind.io3.transport.ServerTransport;
import eastwind.io3.transport.ServerTransportVisitor;

public class RemoteInvocationHandler implements InvocationHandler {

	private String group;
	private ServerRepository serverRepository;
	private ServerConfigurer serverConfigurer;

	public RemoteInvocationHandler(String group, ServerConfigurer netServerConfigurer,
			ServerRepository serverRepository) {
		this.group = group;
		this.serverConfigurer = netServerConfigurer;
		this.serverRepository = serverRepository;
	}

	@Override
	public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
		final InvocationPromise ip = new InvocationPromise();
		InvocationFuture.TL.set(ip);
		
		DefaultServerSelector serverSelector = new DefaultServerSelector(serverConfigurer.getHostIterator(group));
		ServerTransportVisitor transportVisitor = serverRepository.getTransportVisitor(group, true);
		
		Host host = serverSelector.next();
		ServerTransport st = transportVisitor.next(host);
		final Request request = new Request();
		request.setArgs(args);
		
		if (st.getStatus() == 0) {
			st.addShakeListener(new OperationListener<ServerTransport>() {
				@Override
				public void complete(final ServerTransport st) {
					if (st.getStatus() == 1) {
						enquireAndInvoke(method, ip, request, st);
					}
				}

			});
		} else if (st.getStatus() == 1) {
			enquireAndInvoke(method, ip, request, st);
		}
		return ip.get();
//		return returnNull(method);
	}

	private void enquireAndInvoke(final Method method, final InvocationPromise promise, final Request request,
			final ServerTransport st) {
		SettableFuture<HandlerMetaData> metaFuture = st.getHandlerMetaData(method);
		if (metaFuture.isDone()) {
			invoke(promise, request, st, metaFuture.getNow());
		} else {
			metaFuture.addListener(new OperationListener<SettableFuture<HandlerMetaData>>() {
				@Override
				public void complete(SettableFuture<HandlerMetaData> metaFuture) {
					invoke(promise, request, st, metaFuture.getNow());
				}
			}, metaFuture, GlobalExecutor.EVENT_EXECUTOR);
		}
	}
	
	private void invoke(final InvocationPromise promise, final Request request, ServerTransport t, HandlerMetaData meta) {
		request.setName(meta.getName());

		TransmitPromise tp = t.sendAndWaitingForReply(request);
		tp.addListener(new OperationListener<TransmitPromise>() {
			@Override
			public void complete(TransmitPromise t) {
				promise.set(t.getNow());
			}
		}, tp, GlobalExecutor.EVENT_EXECUTOR);
	}

	private Object returnNull(Method method) {
		Class<?> type = method.getReturnType();
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
