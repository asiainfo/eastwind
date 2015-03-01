package boc.message.nioclient;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

import boc.message.common.Notice;
import boc.message.common.NoticeHandler;
import boc.message.common.NoticeHandlerManager;
import boc.message.common.RequestFuture;
import boc.message.common.RequestFuturePool;
import boc.message.common.Respone;
import boc.message.common.ShutdownObj;

@Sharable
public class ClientInboundHandler extends SimpleChannelInboundHandler<Object> {

	private ChannelGuard channelGuard;
	private RequestFuturePool requestPool;
	private NoticeHandlerManager noticeHandlerManager;
	
	public ClientInboundHandler(RequestFuturePool requestPool, ChannelGuard channelGuard, NoticeHandlerManager noticeHandlerManager) {
		this.requestPool = requestPool;
		this.channelGuard = channelGuard;
		this.noticeHandlerManager = noticeHandlerManager;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof Respone) {
			Respone respone = (Respone) msg;
			RequestFuture<?> requestFuture = requestPool.remove(respone.getId());
			if (respone.getResult() instanceof ShutdownObj) {
				channelGuard.setShutdown(requestFuture.getHost());
				requestFuture.fail();
			} else {
				requestFuture.done(respone);j
			}
		} else if (msg instanceof Notice) {
			Notice notice = (Notice) msg;
			List<NoticeHandler> handlers = noticeHandlerManager.getHandlers(notice.getType());
			for (NoticeHandler handler : handlers) {
				handler.handle(notice);
			}
		}
	}

}