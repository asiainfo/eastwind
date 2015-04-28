package eastwind.io.server;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

import eastwind.io.ChannelAttr;
import eastwind.io.Session;
import eastwind.io.common.Ping;
import eastwind.io.common.Request;
import eastwind.io.common.Respone;
import eastwind.io.common.ShutdownObj;

@Sharable
public class ServerInboundHandler extends SimpleChannelInboundHandler<Object> {

	private List<Filter> filters;
	private ProviderManager providerManager;
	private ServerCount serverCount;

	public ServerInboundHandler(List<Filter> filters, ProviderManager providerManager, ServerCount serverCount) {
		this.filters = filters;
		this.providerManager = providerManager;
		this.serverCount = serverCount;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("connected from:" + ctx.channel().remoteAddress());
		serverCount.incrementClientCount();
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("deconnected from:" + ctx.channel().remoteAddress());
		serverCount.decrementClientCount();
		super.channelInactive(ctx);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		Session session = ChannelAttr.get(ctx.channel(), ChannelAttr.SESSION);
		if (session != null) {
			session.refreshAccessedTime();
		}

		if (msg instanceof Ping) {
			ctx.writeAndFlush(Ping.instance);

		} else if (msg instanceof Request) {
			Request request = (Request) msg;

			// shutdown
			if (serverCount.isShutdown()) {
				Respone<Object> respone = new Respone<Object>(request.getId());
				respone.setResult(new ShutdownObj());
				ctx.channel().writeAndFlush(respone);
				return;
			}

			try {
				ChannelAttr.CHANNEL_TL.set(ctx.channel());
				Session.setSession(session);
				serverCount.incrementHandlingCount();
				handleRequest(ctx, request);
			} finally {
				ChannelAttr.CHANNEL_TL.set(null);
				Session.setSession(null);
				serverCount.decrementHandlingCount();
			}
		}
	}

	private void handleRequest(ChannelHandlerContext ctx, Request request) {
		ProviderHandler handler = providerManager.get(request.getType());

		FilterChain filterChain = new FilterChain(handler, ctx.channel(), filters, request);
		filterChain.doNextFilter();

		Respone<Object> respone = new Respone<Object>(request.getId());
		respone.setResult(filterChain.getResult());
		if (filterChain.getTh() != null) {
			respone.setTh(filterChain.getTh());
		}
		ctx.writeAndFlush(respone);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (cause.getMessage().contains("远程主机强迫关闭了一个现有的连接")) {
			return;
		}
		super.exceptionCaught(ctx, cause);
	}
}