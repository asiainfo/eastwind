package eastwind.io.invocation;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import eastwind.io.TransmitPromise;
import eastwind.io.model.ProviderMetaData;
import eastwind.io.model.Request;
import eastwind.io.support.GlobalExecutor;
import eastwind.io.support.OperationListener;
import eastwind.io.support.SettableFuture;
import eastwind.io.transport.Node;
import eastwind.io.transport.ServerGroup;
import eastwind.io.transport.ServerRepository;
import eastwind.io.transport.ServerTransport;

public abstract class AbstractInvocationHandler<T> {

	protected String group;
	protected ServerRepository serverRepository;

	protected AbstractInvocationHandler(String group, ServerRepository serverRepository) {
		this.group = group;
		this.serverRepository = serverRepository;
	}

	protected abstract boolean isSmart(T context);

	protected abstract Type getReturnType(T context);

	protected abstract SettableFuture<ProviderMetaData> getProviderMetaData(T context, ServerTransport st);

	protected InvocationPromise invoke(final T context, final Object[] args) throws Throwable {
		InvocationInfo ii = new InvocationInfo(group, args);
		final InvocationPromise ip = new InvocationPromise(ii);
		InvocationPromise.TL.set(ip);

		ServerTransport st = null;
		final Request request = new Request();
		request.setArgs(args);
		request.setBinary(isSmart(context));
		request.setReturnType(getReturnType(context));

		ServerGroup serverGroup = serverRepository.getServerGroup(group);
		ServerSelector serverSelector = null;
		for (boolean last = false; !last;) {
			if (serverSelector == null) {
				serverSelector = serverGroup.next(null, ii);
			} else if (serverSelector instanceof RouteServerSelector) {
				serverSelector = serverGroup.next((RouteServerSelector) serverSelector, ii);
			}
			if (serverSelector == null) {
				serverSelector = serverGroup.getDefaultSelector();
				last = true;
			}
			
			Set<Node> exculsions = new HashSet<Node>();
		}

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
		SettableFuture<ProviderMetaData> metaFuture = getProviderMetaData(context, st);
		if (metaFuture.isDone()) {
			invoke(promise, request, st, metaFuture.getNow());
		} else {
			metaFuture.addListener(new OperationListener<SettableFuture<ProviderMetaData>>() {
				@Override
				public void complete(SettableFuture<ProviderMetaData> metaFuture) {
					invoke(promise, request, st, metaFuture.getNow());
				}
			}, metaFuture, GlobalExecutor.EVENT_EXECUTOR);
		}
	}

	private void invoke(final InvocationPromise promise, final Request request, ServerTransport st,
			ProviderMetaData meta) {
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
