package eastwind.io2;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

public class AbstractNetworkTrafficTransport extends AbstractTransport implements NetworkTrafficTransport {

	protected SettableFuture<Transport> shakeFuture = new SettableFuture<Transport>();
	
	public AbstractNetworkTrafficTransport(ChannelFuture future) {
		super(future);
	}

	@Override
	public boolean isShaked() {
		return shakeFuture.isDone();
	}

	@Override
	public void addShakeListener(Listener<Transport> listener) {
		addListener(shakeFuture, listener);
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
}
