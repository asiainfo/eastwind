package eastwind.io2;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;


public class AcceptorTransport extends AbstractTransport {

	private ChannelFuture future;
	
	public AcceptorTransport(ChannelFuture future) {
		this.future = future;
		TransportInboundHandler.setTransport(future.channel(), this);
		future.addListener(new GenericFutureListener<ChannelFuture>() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					activeFuture.set(AcceptorTransport.this);
				} else if (future.isCancelled()) {
					activeFuture.cancel(true);
				} else if (future.cause() != null) {
					activeFuture.setException(future.cause());
					th = future.cause();
				}
			}
		});
	}

	@Override
	protected Channel getChannel() {
		return future.channel();
	}
	
}
