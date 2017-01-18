package eastwind.io2;

import java.net.InetSocketAddress;
import java.util.concurrent.CopyOnWriteArrayList;

import io.netty.channel.Channel;

public abstract class AbstractNetworkTrafficTransport extends AbstractTransport implements NetworkTrafficTransport {

	protected Shake shake;
	protected SettableFuture<Shake> shakeFuture = new SettableFuture<Shake>();
	protected CopyOnWriteArrayList<Listener<NetworkTraffic>> networkTrafficListeners = new CopyOnWriteArrayList<Listener<NetworkTraffic>>();

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
	public boolean post(NetworkTraffic networkTraffic) {
		Channel channel = getChannel();
		if (channel == null || !channel.isActive() || !channel.isWritable()) {
			return false;
		}
		channel.writeAndFlush(networkTraffic);
		return true;
	}

	@Override
	public void push(NetworkTraffic networkTraffic) {
		networkTraffic.setTransport(this);
		if (networkTraffic instanceof Shake) {
			this.shake = (Shake) networkTraffic;
			shakeFuture.set(shake);
		} else {
			for (Listener<NetworkTraffic> listener : networkTrafficListeners) {
				listener.listen(networkTraffic);
			}
		}
	}

	@Override
	public void addNetworkTrafficListener(Listener<NetworkTraffic> listener) {
		this.networkTrafficListeners.add(listener);
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		Channel channel = getChannel();
		return channel == null ? null : (InetSocketAddress) channel.remoteAddress();
	}
}
