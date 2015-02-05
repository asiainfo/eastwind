package boc.message.nioclient;

import boc.message.common.RequestFuture;
import boc.message.common.RequestFuturePool;
import boc.message.common.Respone;
import boc.message.common.ShutdownObj;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@Sharable
public class ClientInboundHandler extends SimpleChannelInboundHandler<Respone<?>> {

	private ChannelGuard channelGuard;
	private RequestFuturePool requestPool;

	public ClientInboundHandler(RequestFuturePool requestPool, ChannelGuard channelGuard) {
		this.requestPool = requestPool;
		this.channelGuard = channelGuard;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Respone msg) throws Exception {
		RequestFuture<?> requestFuture = requestPool.remove(msg.getId());
		if (msg.getResult() instanceof ShutdownObj) {
			channelGuard.setShutdown(requestFuture.getHost());
			requestFuture.fail();
		} else {
			requestFuture.done(msg);
		}
	}

}