package eastwind.io3.transport;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentMap;

import eastwind.io3.Sequence;
import eastwind.io3.TransmitPromise;
import eastwind.io3.TransmitSustainer;
import eastwind.io3.obj.HandlerEnquire;
import eastwind.io3.obj.HandlerMetaData;
import eastwind.io3.obj.Host;
import eastwind.io3.obj.Unique;
import eastwind.io3.obj.UniqueHolder;
import eastwind.io3.support.OperationListener;
import eastwind.io3.support.SettableFuture;

public class ServerTransport {

	private String id;
	private int status;
	private Host host;
	private String group;
	private String actualGroup;
	private String uuid;
	private WeakReference<Channel> channelRef;
	private SettableFuture<Integer> shakeFuture;
	private TransmitSustainer transmitSustainer;
	private Sequence sequence;
	private ConcurrentMap<Method, SettableFuture<HandlerMetaData>> handlerMetaDatas;

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

	private final class EnquireListener implements OperationListener<TransmitPromise<HandlerMetaData>> {
		private final Method method;
		private final SettableFuture<HandlerMetaData> metaFuture;
	
		private EnquireListener(Method method, SettableFuture<HandlerMetaData> metaFuture) {
			this.method = method;
			this.metaFuture = metaFuture;
		}
	
		@Override
		public void complete(TransmitPromise<HandlerMetaData> t) {
			HandlerMetaData meta = t.getNow();
			meta.setMethod(method);
			metaFuture.set(meta);
		}
	}

	public SettableFuture<HandlerMetaData> getHandlerMetaData(final Method method) {
		SettableFuture<HandlerMetaData> metaFuture = handlerMetaDatas.get(method);
		if (metaFuture != null) {
			return metaFuture;
		}

		metaFuture = new SettableFuture<HandlerMetaData>();
		SettableFuture<HandlerMetaData> absent = handlerMetaDatas.putIfAbsent(method, metaFuture);

		if (absent == null) {
			HandlerEnquire enquire = createHandlerEnquire(method);
			UniqueHolder holder = UniqueHolder.hold(enquire);

			@SuppressWarnings("unchecked")
			TransmitPromise<HandlerMetaData> tp = sendAndWaitingForReply(holder);
			tp.addListener(new EnquireListener(method, metaFuture));
			return metaFuture;
		} else {
			return absent;
		}
	}

	private HandlerEnquire createHandlerEnquire(final Method method) {
		HandlerEnquire hd = new HandlerEnquire();
		hd.setInterf(method.getDeclaringClass().getName());
		hd.setMethod(method.getName());
		Class<?>[] cls = method.getParameterTypes();
		String[] pts = new String[cls.length];
		for (int i = 0; i < cls.length; i++) {
			pts[i] = cls[i].getCanonicalName();
		}
		hd.setParameterTypes(pts);
		return hd;
	}

	public void addShakeListener(final OperationListener<ServerTransport> listener) {
		getShakeFuture().addListener(new Runnable() {
			@Override
			public void run() {
				listener.complete(ServerTransport.this);
			}
		});
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
		shakeFuture.set(1);
	}

	public void setHandlerMetaDatas(ConcurrentMap<Method, SettableFuture<HandlerMetaData>> handlerMetaDatas) {
		this.handlerMetaDatas = handlerMetaDatas;
	}

	public void setTransmitSustainer(TransmitSustainer transmitSustainer) {
		this.transmitSustainer = transmitSustainer;
	}

	public void setSequence(Sequence sequence) {
		this.sequence = sequence;
	}

	private Channel getChannel() {
		return channelRef == null ? null : channelRef.get();
	}

	private synchronized SettableFuture<Integer> getShakeFuture() {
		if (shakeFuture == null) {
			shakeFuture = new SettableFuture<Integer>();
		}
		return shakeFuture;
	}
}
