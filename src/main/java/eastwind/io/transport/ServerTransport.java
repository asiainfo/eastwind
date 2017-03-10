package eastwind.io.transport;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

import eastwind.io.Sequencer;
import eastwind.io.TransmitPromise;
import eastwind.io.TransmitSustainer;
import eastwind.io.model.HandlerEnquire;
import eastwind.io.model.JsonEnquire;
import eastwind.io.model.MethodEnquire;
import eastwind.io.model.ProviderMetaData;
import eastwind.io.model.Unique;
import eastwind.io.model.UniqueHolder;
import eastwind.io.support.GlobalExecutor;
import eastwind.io.support.OperationListener;
import eastwind.io.support.SettableFuture;

public class ServerTransport {

	private String id;
	private int status;
	private Node node;
	private String group;
	private String uuid;
	private String version;
	private WeakReference<Channel> channelRef;
	private SettableFuture<ServerTransport> shakeFuture;
	private TransmitSustainer transmitSustainer;
	private Sequencer sequence;
	private ProviderMetaDataVisitor providerMetaDataVisitor;

	public ServerTransport(String group, Node node, Channel channel) {
		this.group = group;
		this.node = node;
		this.channelRef = new WeakReference<Channel>(channel);
		this.id = channel.id().asShortText();
	}

	public String getId() {
		return id;
	}

	public String getGroup() {
		return group;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setGroup(String group) {
		this.group = group;
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

	public SettableFuture<ProviderMetaData> getProviderMetaData(Method method) {
		MethodEnquireAdapter methodEnquireAdapter = new MethodEnquireAdapter(method);
		return getProviderMetaData0(methodEnquireAdapter);
	}
	
	public SettableFuture<ProviderMetaData> getProviderMetaData(String name) {
		JsonEnquireAdapter jsonEnquireAdapter = new JsonEnquireAdapter(name);
		return getProviderMetaData0(jsonEnquireAdapter);
	}
	
	private SettableFuture<ProviderMetaData> getProviderMetaData0(ProviderMetaDataEnquireAdapter handlerEnquireAdapter) {
		if (providerMetaDataVisitor != null) {
			SettableFuture<ProviderMetaData> future = handlerEnquireAdapter.getAbsent();
			if (future != null) {
				return future;
			}
		}

		SettableFuture<ProviderMetaData> future = handlerEnquireAdapter.createHandlerMetaData();

		if (future != null) {
			HandlerEnquire enquire = handlerEnquireAdapter.createEnquire();
			UniqueHolder holder = UniqueHolder.hold(enquire);

			OperationListener<TransmitPromise<ProviderMetaData>> enquireListener = handlerEnquireAdapter
					.createEnquireListener();
			if (status == 1) {
				@SuppressWarnings("unchecked")
				TransmitPromise<ProviderMetaData> tp = sendAndWaitingForReply(holder);
				tp.addListener(enquireListener, tp, GlobalExecutor.SINGLE_EXECUTOR);
			} else {
				addShakeListener(new Shake2EnquireListener(handlerEnquireAdapter, holder, enquireListener));
			}
			return future;
		} else {
			return handlerEnquireAdapter.getAbsent();
		}
	}

	public void addShakeListener(final OperationListener<ServerTransport> listener) {
		getShakeFuture().addListener(listener, this, GlobalExecutor.SINGLE_EXECUTOR);
	}

	public void addCloseListener() {

	}

	public Node getNode() {
		return node;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
		shakeFuture.set(this);
	}

	public void setProviderMetaDataVisitor(ProviderMetaDataVisitor providerMetaDataVisitor) {
		this.providerMetaDataVisitor = providerMetaDataVisitor;
	}

	public void setTransmitSustainer(TransmitSustainer transmitSustainer) {
		this.transmitSustainer = transmitSustainer;
	}

	public void setSequence(Sequencer sequence) {
		this.sequence = sequence;
	}

	private class Shake2EnquireListener implements OperationListener<ServerTransport> {
	
		private ProviderMetaDataEnquireAdapter handlerEnquireAdapter;
		private UniqueHolder holder;
		private OperationListener<TransmitPromise<ProviderMetaData>> enquireListener;
	
		public Shake2EnquireListener(ProviderMetaDataEnquireAdapter handlerEnquireAdapter, UniqueHolder holder,
				OperationListener<TransmitPromise<ProviderMetaData>> enquireListener) {
			this.handlerEnquireAdapter = handlerEnquireAdapter;
			this.holder = holder;
			this.enquireListener = enquireListener;
		}
	
		@Override
		public void complete(ServerTransport st) {
			handlerEnquireAdapter.putIfNeeded();
			@SuppressWarnings("unchecked")
			TransmitPromise<ProviderMetaData> promise = sendAndWaitingForReply(holder);
			promise.addListener(enquireListener, promise, GlobalExecutor.SINGLE_EXECUTOR);
		}
	
	}

	abstract class ProviderMetaDataEnquireAdapter {
		protected SettableFuture<ProviderMetaData> future;
	
		protected abstract SettableFuture<ProviderMetaData> get();
	
		protected abstract SettableFuture<ProviderMetaData> putIfAbsent();
	
		public abstract HandlerEnquire createEnquire();
	
		public abstract OperationListener<TransmitPromise<ProviderMetaData>> createEnquireListener();
	
		public SettableFuture<ProviderMetaData> getAbsent() {
			return providerMetaDataVisitor == null ? null : get();
		}
	
		public SettableFuture<ProviderMetaData> createHandlerMetaData() {
			future = new SettableFuture<ProviderMetaData>();
			SettableFuture<ProviderMetaData> absent = null;
			if (providerMetaDataVisitor != null) {
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

	class MethodEnquireAdapter extends ProviderMetaDataEnquireAdapter {
	
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
		public OperationListener<TransmitPromise<ProviderMetaData>> createEnquireListener() {
			return new MethodEnquireListener(method, future);
		}
	
		@Override
		protected SettableFuture<ProviderMetaData> get() {
			return providerMetaDataVisitor.get(method);
		}
	
		@Override
		protected SettableFuture<ProviderMetaData> putIfAbsent() {
			return providerMetaDataVisitor.putIfAbsent(method, future);
		}
	}

	private final class MethodEnquireListener implements OperationListener<TransmitPromise<ProviderMetaData>> {
		private final Method method;
		private final SettableFuture<ProviderMetaData> future;
	
		private MethodEnquireListener(Method method, SettableFuture<ProviderMetaData> future) {
			this.method = method;
			this.future = future;
		}
	
		@Override
		public void complete(TransmitPromise<ProviderMetaData> t) {
			ProviderMetaData meta = t.getNow();
			meta.setMethod(method);
			future.set(meta);
		}
	}

	class JsonEnquireAdapter extends ProviderMetaDataEnquireAdapter {
	
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
		public OperationListener<TransmitPromise<ProviderMetaData>> createEnquireListener() {
			return new JsonEnquireListener(future);
		}
	
		@Override
		protected SettableFuture<ProviderMetaData> get() {
			return providerMetaDataVisitor.get(name);
		}
	
		@Override
		protected SettableFuture<ProviderMetaData> putIfAbsent() {
			return providerMetaDataVisitor.putIfAbsent(name, future);
		}
	
	}

	private final class JsonEnquireListener implements OperationListener<TransmitPromise<ProviderMetaData>> {
		private final SettableFuture<ProviderMetaData> future;
	
		private JsonEnquireListener(SettableFuture<ProviderMetaData> future) {
			this.future = future;
		}
	
		@Override
		public void complete(TransmitPromise<ProviderMetaData> t) {
			ProviderMetaData meta = t.getNow();
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
