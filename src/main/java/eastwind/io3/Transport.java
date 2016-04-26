package eastwind.io3;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;

import java.lang.ref.WeakReference;

public class Transport extends Application {

	static final int NEW = 0;
	static final int CONNECTING = 1;
	static final int HANDSHAKE = 2;

	static final int CONNECT_FAIL = 7;
	static final int HANDSHAKE_FAIL = 8;
	static final int SUSPEND = 9;

	static final int READY = 100;

	protected WeakReference<Channel> channelRef;
	protected int state;
	protected TransportContext transportContext;
	protected RemoteApplication remoteApplication;

	public Transport(String group, TransportContext transportContext) {
		super(group);
		this.transportContext = transportContext;
	}

	public boolean isActive() {
		Channel channel = getChannel();
		return channel != null && channel.isActive() && state == READY;
	}

	public void publish(Object message) {
		getChannel().writeAndFlush(message);
	}

	@SuppressWarnings("rawtypes")
	public TransportPromise publish(Unique message, Object attach) {
		final TransportPromise tp = new TransportPromise();
		long id = transportContext.getSequence().get();
		message.setId(id);
		tp.setMessage(message);
		tp.setId(id);
		tp.setAttach(attach);
		transportContext.add(tp);
		publish0(message, tp);
		return tp;
	}

	@SuppressWarnings("rawtypes")
	protected void publish0(Unique message, final TransportPromise tp) {
		Channel channel = getChannel();
		if (channel == null || !channel.isActive()) {
			tp.failed(null);
		} else {
			channel.writeAndFlush(message).addListener(new GenericFutureListener<ChannelFuture>() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (future.isSuccess()) {
						transportContext.add(tp);
					} else {
						tp.failed(future.cause());
					}
				}
			});
		}
	}

	public void handshake(Handshake hs, RemoteApplication remoteApplication) {
		super.uuid = hs.getMyUuid();
		super.group = hs.getGroup();
		this.remoteApplication = remoteApplication;
		this.state = READY;
		handshake0(hs);
	}

	protected void handshake0(Handshake hs) {

	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public RemoteApplication getRemoteApplication() {
		return remoteApplication;
	}

	public void setRemoteApplication(RemoteApplication remoteApplication) {
		this.remoteApplication = remoteApplication;
	}

	protected void setChannel(Channel channel) {
		this.channelRef = new WeakReference<Channel>(channel);
	}

	protected Channel getChannel() {
		return channelRef == null ? null : channelRef.get();
	}
}
