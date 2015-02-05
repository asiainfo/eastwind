package boc.message.common;

import io.netty.channel.ChannelHandlerContext;

public interface PingListener {

	public void onPing(ChannelHandlerContext ctx);
	
}
