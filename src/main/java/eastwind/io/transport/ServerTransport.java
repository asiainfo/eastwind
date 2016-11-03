package eastwind.io.transport;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

import eastwind.io.Sequence;
import eastwind.io.TransmitPromise;
import eastwind.io.TransmitSustainer;
import eastwind.io.model.HandlerEnquire;
import eastwind.io.model.HandlerMetaData;
import eastwind.io.model.Host;
import eastwind.io.model.JsonEnquire;
import eastwind.io.model.MethodEnquire;
import eastwind.io.model.Unique;
import eastwind.io.model.UniqueHolder;
import eastwind.io.support.GlobalExecutor;
import eastwind.io.support.OperationListener;
import eastwind.io.support.SettableFuture;
import eastwind.io.transport.ServerRepository.ServerHandlerMetaData;

public class ServerTransport {

	private String id;
	private int status;
	private Host host;
	private String group;
	private String actualGroup;
	private String uuid;
	private WeakReference<Channel> channelRef;
	private SettableFuture<ServerTransport> shakeFuture;
	private TransmitSustainer transmitSustainer;
	private Sequence sequence;
	private ServerHandlerMetaData serverHandlerMetaData;

	public ServerTransport(String group, Host host, Channel channel) {
		this.group = group;
		this.host = host;
		this.channelRef = new WeakReference<Channel>(channel);
		this.id = channel.id().asShortText();
	}

	public String getId() {
		return id;
	}

	public String getGroup() {
		return group;
	}

	public String getActualGroup() {
		return actualGroup;
	}

	public void setActualGroup(String actualGroup) {
		this.actualGroup = actualGroup;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public void send(Object message) {
		Channel channel = getChannel();
		if (channel.isActive()) {
			channel.writeAndFlush(message);
		}
	}

	@SuppressWarnings("rawtypes")
	public TransmitPromise sendAndWaitingForReply(Unique message) {
		if (message.getId() == 0) {
			message.setId(sequence.get());
		}
		final TransmitPromise tp = new TransmitPromise<Object>(message);
		transmitSustainer.add(tp);
		Channel channel = getChannel();
		if (channel == null || !channel.isActive()) {
			tp.setStatus(-1);
		} else {
			channel.writeAndFlush(message).addListener(new GenericFutureListener<ChannelFuture>() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (!future.isSuccess()) {
						tp.setStatus(-1);
					}
				}
			});
		}
		return tp;
	}

	public SettableFuture<HandlerMetaData> getHandlerMetaData(Method method) {
		MethodEnquireAdapter methodEnquireAdapter = new MethodEnquireAdapter(method);
		return getHandlerMetaData0(methodEnquireAdapter);
	}
	
	public SettableFuture<HandlerMetaData> getHandlerMetaData(String name) {
		JsonEnquireAdapter jsonEnquireAdapter = new JsonEnquireAdapter(name);
		return getHandlerMetaData0(jsonEnquireAdapter);
	}
	
	private SettableFuture<HandlerMetaData> getHandlerMetaData0(HandlerEnquireAdapter handlerEnquireAdapter) {
		if (serverHandlerMetaData != null) {
			SettableFuture<HandlerMetaData> future = handlerEnquireAdapter.getAbsent();
			if (future != null) {
				return future;
			}
		}

		SettableFuture<HandlerMetaData> future = handlerEnquireAdapter.createHandlerMetaData();

		if (future != null) {
			HandlerEnquire enquire = handlerEnquireAdapter.createEnquire();
			UniqueHolder holder = UniqueHolder.hold(enquire);

			OperationListener<TransmitPromise<HandlerMetaData>> enquireListener = handlerEnquireAdapter
					.createEnquireListener();
			if (status == 1) {
				@SuppressWarnings("unchecked")
				TransmitPromise<HandlerMetaData> tp = sendAndWaitingForReply(holder);
				tp.addListener(enquireListener, tp, GlobalExecutor.SERIAL_EXECUTOR);
			} else {
				getShakeFuture().addListener(new ShakeEnquireListener(handlerEnquireAdapter, holder, enquireListener),
						this, GlobalExecutor.SERIAL_EXECUTOR);
			}
			return future;
		} else {
			return handlerEnquireAdapter.getAbsent();
		}
	}

	public void addShakeListener(final OperationListener<ServerTransport> listener) {
		getShakeFuture().addListener(listener, this, GlobalExecutor.SERIAL_EXECUTOR);
	}

	public void addCloseListener() {

	}

	public int getStatus() {
		return status;
	}

	public Host getHost() {
		return host;
	}

	public void setStatus(int status) {
		this.status = status;
		shakeFuture.set(this);
	}

	public void setServerHandlerMetaData(ServerHandlerMetaData serverHandlerMetaData) {
		this.serverHandlerMetaData = serverHandlerMetaData;
	}

	public void setTransmitSustainer(TransmitSustainer transmitSustainer) {
		this.transmitSustainer = transmitSustainer;
	}

	public void setSequence(Sequence sequence) {
		this.sequence = sequence;
	}

	private class ShakeEnquireListener implements OperationListener<ServerTransport> {
	
		private HandlerEnquireAdapter handlerEnquireAdapter;
		private UniqueHolder holder;
		private OperationListener<TransmitPromise<HandlerMetaData>> enquireListener;
	
		public ShakeEnquireListener(HandlerEnquireAdapter handlerEnquireAdapter, UniqueHolder holder,
				OperationListener<TransmitPromise<HandlerMetaData>> enquireListener) {
			this.handlerEnquireAdapter = handlerEnquireAdapter;
			this.holder = holder;
			this.enquireListener = enquireListener;
		}
	
		@Override
		public void complete(ServerTransport st) {
			handlerEnquireAdapter.putIfNeeded();
			@SuppressWarnings("unchecked")
			TransmitPromise<HandlerMetaData> promise = sendAndWaitingForReply(holder);
			promise.addListener(enquireListener, promise, GlobalExecutor.SERIAL_EXECUTOR);
		}
	
	}

	abstract class HandlerEnquireAdapter {
		protected SettableFuture<HandlerMetaData> future;
	
		protected abstract SettableFuture<HandlerMetaData> get();
	
		protected abstract SettableFuture<HandlerMetaData> putIfAbsent();
	
		public abstract HandlerEnquire createEnquire();
	
		public abstract OperationListener<TransmitPromise<HandlerMetaData>> createEnquireListener();
	
		public SettableFuture<HandlerMetaData> getAbsent() {
			return serverHandlerMetaData == null ? null : get();
		}
	
		public SettableFuture<HandlerMetaData> createHandlerMetaData() {
			future = new SettableFuture<HandlerMetaData>();
			SettableFuture<HandlerMetaData> absent = null;
			if (serverHandlerMetaData != null) {
				absent = putIfAbsent();
			}
			if (absent == null) {
				return future;
			} else {
				future = null;
				return null;
			}
		}
	
		public void putIfNeeded() {
			if (future != null) {
				putIfAbsent();
			}
		}
	}

	class MethodEnquireAdapter extends HandlerEnquireAdapter {
	
		private Method method;
	
		public MethodEnquireAdapter(Method method) {
			this.method = method;
		}
	
		@Override
		public HandlerEnquire createEnquire() {
			MethodEnquire enquire = new MethodEnquire();
			enquire.setInterf(method.getDeclaringClass().getName());
			enquire.setMethod(method.getName());
			Class<?>[] cls = method.getParameterTypes();
			String[] pts = new String[cls.length];
			for (int i = 0; i < cls.length; i++) {
				pts[i] = cls[i].getCanonicalName();
			}
			enquire.setParameterTypes(pts);
			return enquire;
		}
	
		@Override
		public OperationListener<TransmitPromise<HandlerMetaData>> createEnquireListener() {
			return new MethodEnquireListener(method, future);
		}
	
		@Override
		protected SettableFuture<HandlerMetaData> get() {
			return serverHandlerMetaData.get(method);
		}
	
		@Override
		protected SettableFuture<HandlerMetaData> putIfAbsent() {
			return serverHandlerMetaData.putIfAbsent(method, future);
		}
	}

	private final class MethodEnquireListener implements OperationListener<TransmitPromise<HandlerMetaData>> {
		private final Method method;
		private final SettableFuture<HandlerMetaData> future;
	
		private MethodEnquireListener(Method method, SettableFuture<HandlerMetaData> future) {
			this.method = method;
			this.future = future;
		}
	
		@Override
		public void complete(TransmitPromise<HandlerMetaData> t) {
			HandlerMetaData meta = t.getNow();
			meta.setMethod(method);
			future.set(meta);
		}
	}

	class JsonEnquireAdapter extends HandlerEnquireAdapter {
	
		private String name;
	
		public JsonEnquireAdapter(String name) {
			this.name = name;
		}
	
		@Override
		public HandlerEnquire createEnquire() {
			JsonEnquire enquire = new JsonEnquire();
			enquire.setName(name);
			return enquire;
		}
	
		@Override
		public OperationListener<TransmitPromise<HandlerMetaData>> createEnquireListener() {
			return new JsonEnquireListener(future);
		}
	
		@Override
		protected SettableFuture<HandlerMetaData> get() {
			return serverHandlerMetaData.get(name);
		}
	
		@Override
		protected SettableFuture<HandlerMetaData> putIfAbsent() {
			return serverHandlerMetaData.putIfAbsent(name, future);
		}
	
	}

	private final class JsonEnquireListener implements OperationListener<TransmitPromise<HandlerMetaData>> {
		private final SettableFuture<HandlerMetaData> future;
	
		private JsonEnquireListener(SettableFuture<HandlerMetaData> future) {
			this.future = future;
		}
	
		@Override
		public void complete(TransmitPromise<HandlerMetaData> t) {
			HandlerMetaData meta = t.getNow();
			future.set(meta);
		}
	}

	private Channel getChannel() {
		return channelRef == null ? null : channelRef.get();
	}

	private synchronized SettableFuture<ServerTransport> getShakeFuture() {
		if (shakeFuture == null) {
			shakeFuture = new SettableFuture<ServerTransport>();
		}
		return shakeFuture;
	}
}
