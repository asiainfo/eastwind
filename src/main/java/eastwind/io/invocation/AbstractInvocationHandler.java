package eastwind.io.invocation;

import java.lang.reflect.Type;

import eastwind.io.ServerConfigurer;
import eastwind.io.TransmitPromise;
import eastwind.io.model.HandlerMetaData;
import eastwind.io.model.Host;
import eastwind.io.model.Request;
import eastwind.io.support.GlobalExecutor;
import eastwind.io.support.OperationListener;
import eastwind.io.support.SettableFuture;
import eastwind.io.transport.DefaultServerSelector;
import eastwind.io.transport.ServerRepository;
import eastwind.io.transport.ServerTransport;
import eastwind.io.transport.ServerTransportVisitor;

public abstract class AbstractInvocationHandler<T> {
	
	protected String group;
	protected ServerRepository serverRepository;
	protected ServerConfigurer serverConfigurer;
	
	protected AbstractInvocationHandler(String group, ServerRepository serverRepository, ServerConfigurer serverConfigurer) {
		this.group = group;
		this.serverRepository = serverRepository;
		this.serverConfigurer = serverConfigurer;
	}

	protected abstract boolean isBinary(T context);
	protected abstract Type getReturnType(T context);
	protected abstract SettableFuture<HandlerMetaData> getHandlerMetaData(T context, ServerTransport st);
	
	protected InvocationPromise invoke(final T context, final Object[] args) throws Throwable {
		final InvocationPromise ip = new InvocationPromise();
		InvocationPromise.TL.set(ip);
		
		DefaultServerSelector serverSelector = new DefaultServerSelector(serverConfigurer.getHostIterator(group));
		ServerTransportVisitor transportVisitor = serverRepository.getTransportVisitor(group, true);
		
		Host host = serverSelector.next();
		ServerTransport st = transportVisitor.next(host);
		final Request request = new Request();
		request.setArgs(args);
		request.setBinary(isBinary(context));
		request.setReturnType(getReturnType(context));
		
		if (st.getStatus() == 0) {
			st.addShakeListener(new OperationListener<ServerTransport>() {
				@Override
				public void complete(final ServerTransport st) {
					if (st.getStatus() == 1) {
						enquireAndInvoke(context, ip, request, st);
					}
				}

			});
		} else if (st.getStatus() == 1) {
			enquireAndInvoke(context, ip, request, st);
		}
		return ip;
	}
	
	private void enquireAndInvoke(final T context, final InvocationPromise promise, final Request request,
			final ServerTransport st) {
		SettableFuture<HandlerMetaData> metaFuture = getHandlerMetaData(context, st);
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
	
	private void invoke(final InvocationPromise promise, final Request request, ServerTransport st, HandlerMetaData meta) {
		request.setName(meta.getName());

		TransmitPromise tp = st.sendAndWaitingForReply(request);
		tp.addListener(new OperationListener<TransmitPromise>() {
			@Override
			public void complete(TransmitPromise t) {
				promise.set(t.getNow());
			}
		}, tp, GlobalExecutor.EVENT_EXECUTOR);
	}
}
