package eastwind.io2;

import io.netty.channel.ChannelFuture;

public class LocalTransport extends AbstractTransport {

	public LocalTransport(ChannelFuture future) {
		super(future);
	}

}
