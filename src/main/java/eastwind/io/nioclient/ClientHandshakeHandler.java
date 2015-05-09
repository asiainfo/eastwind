package eastwind.io.nioclient;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Maps;

import eastwind.io.ChannelAttr;
import eastwind.io.common.Handshake;

public class ClientHandshakeHandler extends SimpleChannelInboundHandler<Handshake> {

	private String app;

	public ClientHandshakeHandler(String app) {
		this.app = app;
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
		Channel channel = ctx.channel();
		if (msg.getStep() == 1) {
			ClientHandshaker clientHandshaker = getClientHandshaker(channel);
			Map<String, Object> out = Maps.newHashMap();
			clientHandshaker.prepare(msg.getApp(), channel, Collections.unmodifiableMap(msg.getAttributes()), out);
			Handshake handshake = new Handshake();
			handshake.setApp(app);
			handshake.setStep(2);
			handshake.setAttributes(out);
			ctx.writeAndFlush(handshake);
		} else if (msg.getStep() == 3) {
			ClientHandshaker clientHandshaker = getClientHandshaker(channel);
			clientHandshaker.handshakeComplete(channel, Collections.unmodifiableMap(msg.getAttributes()));
			ChannelPromise handshakePromise = ChannelAttr.get(channel, ChannelAttr.HANDSHAKE_PROMISE);
			handshakePromise.setSuccess();
		}
	}

	private ClientHandshaker getClientHandshaker(Channel channel) {
		return ChannelAttr.get(channel, ChannelAttr.CLIENT_HANDSHAKE);
	}
}
