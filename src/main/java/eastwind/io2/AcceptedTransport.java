package eastwind.io2;

import io.netty.channel.Channel;


public class AcceptedTransport extends AbstractNetworkTrafficTransport {

	private Channel channel;
	
	public AcceptedTransport(Channel channel) {
		this.channel = channel;
		activeFuture.set(AcceptedTransport.this);
	}

	@Override
	protected Channel getChannel() {
		return channel;
	}
}
