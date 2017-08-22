package eastwind.io2;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractNetworkTrafficTransport extends AbstractTransport implements NetworkTrafficTransport {

	protected Shake shake;
	protected SettableFuture<Shake> shakeFuture = new SettableFuture<Shake>();
	protected CopyOnWriteArrayList<Listener<NetworkTraffic>> pushListeners = new CopyOnWriteArrayList<Listener<NetworkTraffic>>();

	@Override
	public boolean isShaked() {
		return shakeFuture.isDone();
	}

	@Override
	public Shake getShake() {
		return shake;
	}

	@Override
	public void addShakeListener(final Listener<Shake> listener) {
		shakeFuture.addListener(new Runnable() {
			@Override
			public void run() {
				listener.listen(shake);
			}
		}, getExecutor());
	}

	@Override
	public void post(NetworkTraffic networkTraffic) {
		Channel channel = getChannel();
		if (channel != null && channel.isActive() && channel.isWritable()) {
			channel.writeAndFlush(networkTraffic);
		}
	}

	@Override
	public Exchange exchange(Request request) {
		Channel channel = getChannel();
		if (channel != null && channel.isActive() && channel.isWritable()) {
			MasterPeer peer = (MasterPeer) peer();
			Exchange exchange = peer.exchange(request);
			channel.writeAndFlush(request);
			return exchange;
		}
		return null;
	}
	
	@Override
	public void push(NetworkTraffic networkTraffic) {
		networkTraffic.setTransport(this);
		if (networkTraffic instanceof Shake) {
			this.shake = (Shake) networkTraffic;
			shakeFuture.set(shake);
		} else {
			for (Listener<NetworkTraffic> listener : pushListeners) {
				listener.listen(networkTraffic);
			}
		}
	}

	@Override
	public void addPushListener(Listener<NetworkTraffic> listener) {
		this.pushListeners.add(listener);
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		Channel channel = getChannel();
		return channel == null ? null : (InetSocketAddress) channel.remoteAddress();
	}
}
