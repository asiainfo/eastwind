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
import eastwind.io.common.InterfAb;

public class ClientHandshakeHandler extends SimpleChannelInboundHandler<Handshake> {

	private String app;

	public ClientHandshakeHandler(String app) {
		this.app = app;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Handshake msg) throws Exception {
		Channel channel = ctx.channel();
		String remoteApp = ChannelAttr.get(channel, ChannelAttr.REMOTE_APP);
		InterfAb interfAb = ChannelAttr.get(channel, ChannelAttr.INTERF_AB);
		if (msg.getStep() == 1) {
			interfAb.ackUuid(msg.getUuid());
			if (!msg.getApp().equals(remoteApp)) {
				// TODO
			}

			ClientHandshaker clientHandshaker = getClientHandshaker(channel);
			Map<String, Object> out = Maps.newHashMap();
			clientHandshaker.prepare(msg.getApp(), channel, Collections.unmodifiableMap(msg.getAttributes()), out);

			Handshake handshake = new Handshake();
			handshake.setApp(app);
			handshake.setStep(2);
			handshake.setAttributes(out);
			ctx.writeAndFlush(handshake);
		} else if (msg.getStep() == 3) {
			interfAb.ackUuid(msg.getUuid());
			if (!msg.getApp().equals(remoteApp)) {
				// TODO
			}

			ClientHandshaker clientHandshaker = getClientHandshaker(channel);
			if (clientHandshaker != null) {
				clientHandshaker.handshakeComplete(channel, Collections.unmodifiableMap(msg.getAttributes()));
			}
			ChannelPromise handshakePromise = ChannelAttr.get(channel, ChannelAttr.HANDSHAKE_PROMISE);
			handshakePromise.setSuccess();
		}
	}

	private ClientHandshaker getClientHandshaker(Channel channel) {
		return ChannelAttr.get(channel, ChannelAttr.CLIENT_HANDSHAKE);
	}
}
