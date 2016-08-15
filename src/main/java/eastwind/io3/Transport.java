package eastwind.io3;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;

import java.lang.ref.WeakReference;

public class Transport extends App {

	static final int NEW = 0;
	static final int CONNECTING = 1;
	static final int HANDSHAKE = 2;

	static final int CONNECT_FAIL = 7;
	static final int HANDSHAKE_FAIL = 8;
	static final int SUSPEND = 9;

	static final int READY = 100;

	protected WeakReference<Channel> channelRef;
	protected int state;
	protected PromiseSustainer promiseSustainer;
	protected RemoteApp remoteApplication;

	public Transport(String group, PromiseSustainer promiseSustainer) {
		super(group);
		this.promiseSustainer = promiseSustainer;
	}

	public boolean isReady() {
		Channel channel = getChannel();
		return channel != null && channel.isActive() && state == READY;
	}

	public void send(Object message) {
		getChannel().writeAndFlush(message);
	}

	@SuppressWarnings("rawtypes")
	public TransmitPromise send(Unique message, Object attach) {
		final TransmitPromise tp = new TransmitPromise(this);
		if (message.getId() == 0) {
			message.setId(promiseSustainer.getSequence().get());
		}
		tp.setId(message.getId());
		tp.setMessage(message);
		tp.setAttach(attach);
		send0(tp);
		promiseSustainer.add(tp);
		return tp;
	}

	@SuppressWarnings("rawtypes")
	protected void send0(final TransmitPromise tp) {
		Channel channel = getChannel();
		if (channel == null || !channel.isActive()) {
			tp.failed(null);
		} else {
			channel.writeAndFlush(tp.getMessage()).addListener(new GenericFutureListener<ChannelFuture>() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (future.isSuccess()) {
						promiseSustainer.add(tp);
					} else {
						tp.failed(future.cause());
					}
				}
			});
		}
	}

	public void handshake(Handshake hs, RemoteApp remoteApplication) {
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

	public RemoteApp getRemoteApplication() {
		return remoteApplication;
	}

	public void setRemoteApplication(RemoteApp remoteApplication) {
		this.remoteApplication = remoteApplication;
	}

	protected void setChannel(Channel channel) {
		this.channelRef = new WeakReference<Channel>(channel);
	}

	protected Channel getChannel() {
		return channelRef == null ? null : channelRef.get();
	}
}
