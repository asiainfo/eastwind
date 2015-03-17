package boc.message.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Map;

import boc.message.common.Handshake;

import com.google.common.collect.Maps;

public class ServerHandshakeHandler extends SimpleChannelInboundHandler<Handshake> {

	private ServerHandshaker serverHandshaker;

	public ServerHandshakeHandler(ServerHandshaker serverHandshaker) {
		this.serverHandshaker = serverHandshaker;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		Map<String, Object> out = Maps.newHashMap();
		serverHandshaker.prepare(out);
		Handshake handshake = new Handshake();
		handshake.setStep(serverHandshaker.isMultiStep() ? 1 : 3);
		handshake.setAttributes(out);
		ctx.channel().writeAndFlush(handshake);
		super.channelActive(ctx);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Handshake msg) throws Exception {
		if (msg.getStep() == 2) {
			Map<String, Object> out = Maps.newHashMap();
			serverHandshaker.handshake(msg.getAttributes(), out);
			Handshake handshake = new Handshake();
			handshake.setStep(3);
			handshake.setAttributes(out);
			ctx.writeAndFlush(handshake);
		}
	}

}
