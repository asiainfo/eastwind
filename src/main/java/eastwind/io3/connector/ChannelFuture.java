package eastwind.io3.connector;

import java.util.concurrent.CompletableFuture;

public class ChannelFuture<T> extends CompletableFuture<T> {

	private AbstractChannel channel;

	public ChannelFuture(AbstractChannel channel) {
		this.channel = channel;
	}

	public AbstractChannel getChannel() {
		return channel;
	}
	
}
