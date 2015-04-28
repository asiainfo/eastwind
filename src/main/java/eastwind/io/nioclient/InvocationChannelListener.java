package eastwind.io.nioclient;

import eastwind.io.common.InvocationFuture;
import eastwind.io.common.InvocationFuturePool;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class InvocationChannelListener implements ChannelFutureListener {

	private InvocationFuture<?> invocationFuture;
	private InvocationFuturePool invocationFuturePool;
	
	public InvocationChannelListener(InvocationFuture<?> invocationFuture,
			InvocationFuturePool invocationFuturePool) {
		this.invocationFuture = invocationFuture;
		this.invocationFuturePool = invocationFuturePool;
	}

	@Override
	public void operationComplete(ChannelFuture future) throws Exception {
		if (future.isSuccess()) {
			invocationFuturePool.put(invocationFuture);
			future.channel().writeAndFlush(invocationFuture.getRequest());
		} else if (future.cause() != null) {
			invocationFuture.fail();
		}
	}

}
