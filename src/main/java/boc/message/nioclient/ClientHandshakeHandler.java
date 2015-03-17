package boc.message.nioclient;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Map;

import boc.message.ChannelAttr;
import boc.message.common.Handshake;

import com.google.common.collect.Maps;

public class ClientHandshakeHandler extends SimpleChannelInboundHandler<Handshake> {

	private Map<String, ClientHandshaker> clientHandshakers;

	public ClientHandshakeHandler(Map<String, ClientHandshaker> clientHandshakers) {
		this.clientHandshakers = clientHandshakers;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		if (getClientHandshaker(ctx.channel()) == null) {
			ChannelAttr.get(ctx.channel(), ChannelAttr.HANDSHAKE_PROMISE).setSuccess();
		}
		super.channelActive(ctx);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Handshake msg) throws Exception {
		if (msg.getStep() == 1) {
			ClientHandshaker clientHandshaker = getClientHandshaker(ctx.channel());
			Map<String, Object> out = Maps.newHashMap();
			clientHandshaker.prepare(msg.getAttributes(), out);
			Handshake handshake = new Handshake();
			handshake.setStep(2);
			handshake.setAttributes(out);
			ctx.writeAndFlush(handshake);
		} else if (msg.getStep() == 3) {
			ClientHandshaker clientHandshaker = getClientHandshaker(ctx.channel());
			clientHandshaker.handshakeComplete(msg.getAttributes());
			ChannelPromise handshakePromise = ChannelAttr.get(ctx.channel(), ChannelAttr.HANDSHAKE_PROMISE);
			handshakePromise.setSuccess();
		}
	}

	private ClientHandshaker getClientHandshaker(Channel channel) {
		String app = ChannelAttr.get(channel, ChannelAttr.APP);
		return clientHandshakers.get(app);
	}
}
