package boc.message.server;

import boc.message.common.Request;
import io.netty.channel.ChannelHandlerContext;

public class ExceptionResolver {

	public static Object NONE_RETURN = new Object();

	public Object doResulver(ChannelHandlerContext ctx, Request request, Throwable th) {
		return NONE_RETURN;
	}
	
	public boolean clearStack() {
		return true;
	}
}
