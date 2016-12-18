package eastwind.io2;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;

import java.lang.ref.WeakReference;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

import eastwind.io.support.GlobalExecutor;

public abstract class AbstractTransport implements Transport {

	protected WeakReference<ChannelFuture> futureRef;
	protected EndPoint endPoint;
	protected boolean shaked;
	
	protected CopyOnWriteArrayList<Listener<NetworkTraffic>> networkTrafficListeners;
	protected SettableFuture<Transport> activeFuture = new SettableFuture<Transport>();
	protected SettableFuture<Transport> closeFuture = new SettableFuture<Transport>();
	
	public AbstractTransport(ChannelFuture future) {
		this.futureRef = new WeakReference<ChannelFuture>(future);
		future.channel().attr(Constants.TRANSPORT).set(this);
		future.addListener(new GenericFutureListener<ChannelFuture>() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					activeFuture.set(AbstractTransport.this);
				}
			}
		});
	}

	@Override
	public boolean isActived() {
		ChannelFuture future = futureRef.get();
		return future == null ? false : future.isSuccess() && future.channel().isActive();
	}

	@Override
	public boolean isClosed() {
		return closeFuture.isDone();
	}

	@Override
	public Throwable getTh() {
		ChannelFuture future = futureRef.get();
		if (future != null) {
			return future.cause();
		}
		return null;
	}

	@Override
	public void attach(EndPoint endPoint) {
		this.endPoint = endPoint;
	}

	@Override
	public EndPoint endPoint() {
		return endPoint;
	}

	@Override
	public void addActiveListener(Listener<Transport> listener) {
		addListener(activeFuture, listener);
	}

	@Override
	public void addCloseListener(Listener<Transport> listener) {
		addListener(closeFuture, listener);
	}

	@Override
	public void push(NetworkTraffic networkTraffic) {
		for (Listener<NetworkTraffic> listener : networkTrafficListeners) {
			listener.listen(networkTraffic);
		}
	}

	@Override
	public void addNetworkTrafficListener(Listener<NetworkTraffic> listener) {
		this.networkTrafficListeners.add(listener);
	}
	
	protected void addListener(SettableFuture<Transport> future, Listener<Transport> listener) {
		ChannelFuture cf = futureRef.get();
		Executor executor = future == null ? GlobalExecutor.SINGLE_EXECUTOR : cf.channel().eventLoop();
		future.addListener(new ListenerAdapter(this, listener), executor);
	}
	
	protected Channel getChannel() {
		ChannelFuture channelFuture = futureRef.get();
		return channelFuture == null ? null : channelFuture.channel();
	}
	
	private static class ListenerAdapter implements Runnable {

		Transport transport;
		Listener<Transport> listener;
		
		public ListenerAdapter(Transport transport, Listener<Transport> listener) {
			this.transport = transport;
			this.listener = listener;
		}

		@Override
		public void run() {
			listener.listen(transport);
		}
		
	}
}