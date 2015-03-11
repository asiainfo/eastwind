package boc.message.server;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import boc.message.ChannelStat;
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

	private ProviderManager providerHandlerManager;
	private RequestPool requestPool;

	private ServerContext serverContext;

	public ServerInboundHandler(List<Filter> filters, ExceptionResolver exceptionResolver,
			ServerContext serverContext) {
		this.filters = filters;
		this.exceptionResolver = exceptionResolver;

		this.providerHandlerManager = serverContext.getProviderManager();
		this.requestPool = serverContext.getRequestPool();
		this.serverContext = serverContext;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("channel active:" + ctx.channel().remoteAddress());
		Session session = new Session(ctx.channel());
		requestPool.newGroup(session.getId());
		ChannelStat.set(ctx.channel(), ChannelStat.session, session);
		serverContext.incrementClientCount();
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("channel inactive:" + ctx.channel().remoteAddress());
		int id = ChannelStat.get(ctx.channel(), ChannelStat.session).getId();
		requestPool.delGroup(id);
		serverContext.decrementClientCount();
		super.channelInactive(ctx);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof Ping) {
			ctx.writeAndFlush(Ping.instance);
			
		} else if (msg instanceof Request) {
			Request request = (Request) msg;

			// shutdown
			if (CioServer.shutdown) {
				Respone<Object> respone = new Respone<Object>(request.getId());
				respone.setResult(new ShutdownObj());
				ctx.channel().writeAndFlush(respone);
				return;
			}
			
			Session session = ChannelStat.get(ctx.channel(), ChannelStat.session);
			try {
				Session.setSession(session);
				requestPool.addRequest(session.getId(), request);
				handleRequest(ctx, request);
			} finally {
				Session.setSession(null);
				requestPool.delRequest(session.getId(), request.getId());
			}
		}
	}

	private void handleRequest(ChannelHandlerContext ctx, Request request) {
		ProviderHandler handler = providerHandlerManager.get(request.getType());
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