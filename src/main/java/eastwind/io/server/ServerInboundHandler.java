package eastwind.io.server;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eastwind.io.ChannelAttr;
import eastwind.io.Session;
import eastwind.io.common.Messaging;
import eastwind.io.common.Ping;
import eastwind.io.common.Request;
import eastwind.io.common.Response;
import eastwind.io.common.ShutdownObj;

@Sharable
public class ServerInboundHandler extends SimpleChannelInboundHandler<Object> {

	private static Logger logger = LoggerFactory.getLogger(ServerInboundHandler.class);

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
		logger.info("{}->:connected", ctx.channel().remoteAddress());
		serverCount.incrementClientCount();
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.info("{}->:closed", ctx.channel().remoteAddress());
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

			if (!StringUtils.isNumeric(request.getInterf())) {
				String interf = request.getInterf();
				String id = providerManager.getInterfId(interf);
				Messaging messaging = new Messaging(Messaging.INTERF_ID, 0, new String[] { interf, id });
				ctx.channel().writeAndFlush(messaging);
			}

			// shutdown
			if (serverCount.isShutdown()) {
				Response<Object> respone = new Response<Object>(request.getId());
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
		ProviderHandler handler = providerManager.get(request.getInterf(), request.getName());

		FilterChain filterChain = new FilterChain(handler, ctx.channel(), filters, request);
		filterChain.doNextFilter();

		Response<Object> respone = new Response<Object>(request.getId());
		respone.setResult(filterChain.getResult());
		if (filterChain.getTh() != null) {
			filterChain.getTh().printStackTrace();
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