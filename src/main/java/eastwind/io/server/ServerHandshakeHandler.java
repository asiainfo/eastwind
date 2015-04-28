package eastwind.io.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Maps;

import eastwind.io.common.Handshake;

public class ServerHandshakeHandler extends SimpleChannelInboundHandler<Handshake> {

	private String app;
	private ServerHandshaker serverHandshaker;

	public ServerHandshakeHandler(String app, ServerHandshaker serverHandshaker) {
		this.app = app;
		this.serverHandshaker = serverHandshaker;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		Map<String, Object> out = Maps.newHashMap();
		serverHandshaker.prepare(ctx.channel(), out);
		Handshake handshake = new Handshake();
		handshake.setApp(app);
		handshake.setStep(serverHandshaker.isMultiStep() ? 1 : 3);
		handshake.setAttributes(out);
		ctx.channel().writeAndFlush(handshake);
		super.channelActive(ctx);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Handshake msg) throws Exception {
		if (msg.getStep() == 2) {
			Map<String, Object> out = Maps.newHashMap();
			serverHandshaker.handshake(msg.getApp(), ctx.channel(), Collections.unmodifiableMap(msg.getAttributes()),
					out);
			Handshake handshake = new Handshake();
			handshake.setApp(app);
			handshake.setStep(3);
			handshake.setAttributes(out);
			ctx.writeAndFlush(handshake);
		}
	}

}
