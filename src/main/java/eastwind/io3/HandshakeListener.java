package eastwind.io3;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class HandshakeListener implements ChannelFutureListener {

	private ChannelAware channelAware;

	@Override
	public void operationComplete(ChannelFuture future) throws Exception {
		if (future.isSuccess()) {
		}
	}

}
