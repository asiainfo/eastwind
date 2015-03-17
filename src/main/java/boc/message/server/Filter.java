package boc.message.server;

import boc.message.common.Request;
import boc.message.common.Respone;
import io.netty.channel.ChannelHandlerContext;

public class Filter {

	/**
	 * @param ctx
	 * @param request
	 * @return null or error-code
	 */
	public String beforeProcess(ChannelHandlerContext ctx, Request request) {
		return null;
	}

	public void afterProcess(ChannelHandlerContext ctx, Request request, Respone<?> respone) {

	}
	
}
