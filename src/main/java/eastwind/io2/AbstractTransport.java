package eastwind.io2;

import io.netty.channel.Channel;

import java.util.concurrent.Executor;

import eastwind.io.support.GlobalExecutor;

public abstract class AbstractTransport implements Transport {

	protected Throwable th;
	protected Peer peer;
	
	protected SettableFuture<Transport> activeFuture = new SettableFuture<Transport>();
	protected SettableFuture<Transport> closeFuture = new SettableFuture<Transport>();

	@Override
	public boolean isActived() {
		Channel channel = getChannel();
		return channel == null ? false : channel.isActive();
	}

	public Throwable getTh() {
		return th;
	}

	@Override
	public boolean isClosed() {
		return closeFuture.isDone();
	}

	@Override
	public void attach(Peer endPoint) {
		this.peer = endPoint;
	}

	@Override
	public Peer peer() {
		return peer;
	}

	@Override
	public void addActiveListener(Listener<Transport> listener) {
		activeFuture.addListener(new ListenerAdapter(this, listener), getExecutor());
	}

	@Override
	public void addCloseListener(Listener<Transport> listener) {
		closeFuture.addListener(new ListenerAdapter(this, listener), getExecutor());
	}

	protected Executor getExecutor() {
		Channel channel = getChannel();
		Executor executor = channel == null ? GlobalExecutor.SINGLE_EXECUTOR : channel.eventLoop();
		return executor;
	}

	protected abstract Channel getChannel();
	
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