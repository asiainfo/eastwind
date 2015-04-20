package eastwind.io.nioclient;

import eastwind.io.common.RequestFuture;
import eastwind.io.common.RequestFuturePool;
import eastwind.io.common.Respone;
import eastwind.io.common.ShutdownObj;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@Sharable
public class ClientInboundHandler extends SimpleChannelInboundHandler<Object> {

	private ChannelGuard channelGuard;
	private RequestFuturePool requestPool;
	
	public ClientInboundHandler(RequestFuturePool requestPool, ChannelGuard channelGuard) {
		this.requestPool = requestPool;
		this.channelGuard = channelGuard;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof Respone) {
			Respone respone = (Respone<?>) msg;
			RequestFuture<?> requestFuture = requestPool.remove(respone.getId());
			if (respone.getResult() instanceof ShutdownObj) {
				channelGuard.shutdown(requestFuture.getHost());
				requestFuture.fail();
			} else {
				requestFuture.done(respone);
			}
		}
	}

}