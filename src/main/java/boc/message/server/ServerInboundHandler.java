package boc.message.server;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import boc.message.ChannelAttr;
import boc.message.Session;
import boc.message.common.Ping;
import boc.message.common.Request;
import boc.message.common.Respone;
import boc.message.common.ShutdownObj;

@Sharable
public class ServerInboundHandler extends SimpleChannelInboundHandler<Object> {

	private static StackTraceElement[] emptyStackTraceElement = new StackTraceElement[0];

	private List<Filter> filters;
	private ExceptionResolver exceptionResolver;
	private ProviderManager providerManager;
	private ServerCount serverCount;

	public ServerInboundHandler(List<Filter> filters, ExceptionResolver exceptionResolver,
			ProviderManager providerManager, ServerCount serverCount) {
		this.filters = filters;
		this.providerManager = providerManager;
		this.exceptionResolver = exceptionResolver;
		this.serverCount = serverCount;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		serverCount.incrementClientCount();
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
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
		for (Filter filter : filters) {
			filter.beforeProcess(ctx, request);
		}

		Object result = null;
		Throwable th = null;
		try {
			result = handler.invoke(request.getArgs());
		} catch (InvocationTargetException e) {
			th = e.getCause();
		} catch (Throwable e) {
			th = e;
		}
		if (th != null) {
			th.printStackTrace();
		}
		Respone<Object> respone = new Respone<Object>(request.getId());
		respone.setResult(result);
		if (th != null) {
			respone.setTh(th);
		}

		for (Filter filter : filters) {
			filter.afterProcess(ctx, request, respone);
		}

		if (th != null && exceptionResolver != null) {
			try {
				exceptionResolver.doResulver(ctx, request, th);
			} catch (Throwable e) {
				th = e;
				if (exceptionResolver.clearStack()) {
					th.setStackTrace(emptyStackTraceElement);
				}
				respone.setTh(th);
			}
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