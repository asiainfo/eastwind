package eastwind.io3.connector;

import java.util.concurrent.CompletableFuture;

import io.netty.channel.Channel;

public class AbstractChannel {

	protected Channel channel;
	protected ChannelStat stat = ChannelStat.OPENING;
	protected Throwable th;

	private CompletableFuture<Void> activeFuture = new CompletableFuture<Void>();
	private CompletableFuture<Void> closeFuture = new CompletableFuture<Void>();

	protected AbstractChannel(Channel channel) {
		this.channel = channel;
		this.channel.closeFuture().addListener(future -> {
			stat = ChannelStat.CLOSED;
			closeFuture.complete(null);
		});
	}

	protected void active() {
		this.stat = ChannelStat.OPENED;
		this.activeFuture.complete(null);
	}

	protected void activeFailed(Throwable th) {
		this.stat = ChannelStat.CLOSING;
		this.activeFuture.completeExceptionally(th);
		this.th = th;
	}

	public ChannelFuture<Void> activeFuture() {
		ChannelFuture<Void> cf = new ChannelFuture<>(this);
		activeFuture.thenAccept(b -> cf.complete(b));
		return cf;
	}

	public ChannelFuture<Void> closeFuture() {
		ChannelFuture<Void> cf = new ChannelFuture<>(this);
		closeFuture.thenAccept(v -> cf.complete(v));
		return cf;
	}

	public ChannelStat getStat() {
		return stat;
	}

	public Throwable getTh() {
		return th;
	}

}
