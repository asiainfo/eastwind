package eastwind.io.nioclient;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eastwind.io.ChannelAttr;
import eastwind.io.common.InterfAb;
import eastwind.io.common.InvocationFuture;
import eastwind.io.common.InvocationFuturePool;
import eastwind.io.common.Messaging;
import eastwind.io.common.Respone;
import eastwind.io.common.ShutdownObj;

@Sharable
public class ClientInboundHandler extends SimpleChannelInboundHandler<Object> {

	private static Logger logger = LoggerFactory.getLogger(ClientInboundHandler.class);

	private ChannelGuard channelGuard;
	private InvocationFuturePool requestPool;

	public ClientInboundHandler(InvocationFuturePool invocationPool, ChannelGuard channelGuard) {
		this.requestPool = invocationPool;
		this.channelGuard = channelGuard;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		logger.info("->{}:connected", ctx.channel().remoteAddress());
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.info("->{}:closed", ctx.channel().remoteAddress());
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
		} else if (msg instanceof Messaging) {
			Messaging messaging = (Messaging) msg;
			if (messaging.getType() == Messaging.INTERF_ID) {
				String[] interfId = (String[]) messaging.getData();
				InterfAb interfAb = ChannelAttr.get(ctx.channel(), ChannelAttr.INTERF_AB);
				interfAb.setInterfId(interfId[0], interfId[1]);
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