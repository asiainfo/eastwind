package eastwind.io3;

import io.netty.channel.Channel;

public class Transport {

	private boolean suspend = false;
	private Channel channel;
	private TransportPromise transportPromise = new TransportPromise(this);
	
	public boolean isActive() {
		return getChannel() != null && getChannel().isActive() && !suspend;
	}
	
	public void send(Object object) {
		getChannel().writeAndFlush(object);
	}
	
	public void setSuspend(boolean suspend) {
		this.suspend = suspend;
	}

	protected void setChannel(Channel channel) {
		this.channel = channel;
	}

	protected Channel getChannel() {
		return this.channel;
	}

	public TransportPromise getTransportPromise() {
		return transportPromise;
	}
}
