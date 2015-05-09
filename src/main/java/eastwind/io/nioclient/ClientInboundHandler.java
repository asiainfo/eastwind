package eastwind.io.nioclient;

import eastwind.io.common.InvocationFuture;
import eastwind.io.common.InvocationFuturePool;
import eastwind.io.common.Respone;
import eastwind.io.common.ShutdownObj;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@Sharable
public class ClientInboundHandler extends SimpleChannelInboundHandler<Object> {

	private ChannelGuard channelGuard;
	private InvocationFuturePool requestPool;
	
	public ClientInboundHandler(InvocationFuturePool invocationPool, ChannelGuard channelGuard) {
		this.requestPool = invocationPool;
		this.channelGuard = channelGuard;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("connect ok:" + ctx.channel().remoteAddress());
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("deconnect to:" + ctx.channel().remoteAddress());
		super.channelInactive(ctx);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof Respone) {
			@SuppressWarnings("rawtypes")
			Respone respone = (Respone) msg;
			InvocationFuture<?> invocationFuture = requestPool.remove(respone.getId());
			if (respone.getResult() instanceof ShutdownObj) {
				channelGuard.shutdown(invocationFuture.getHost());
				invocationFuture.fail();
			} else {
				invocationFuture.done(respone);
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (cause.getMessage().contains("远程主机强迫关闭了一个现有的连接")) {
			return;
		}
		super.exceptionCaught(ctx, cause);
	}
}