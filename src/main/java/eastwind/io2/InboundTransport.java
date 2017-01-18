package eastwind.io2;

import io.netty.channel.Channel;


public class InboundTransport extends AbstractNetworkTrafficTransport {

	private Channel channel;
	
	public InboundTransport(Channel channel) {
		this.channel = channel;
		activeFuture.set(InboundTransport.this);
	}

	@Override
	protected Channel getChannel() {
		return channel;
	}
}
