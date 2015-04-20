package eastwind.io.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Map;

import com.google.common.collect.Maps;

import eastwind.io.common.Handshake;

public class ServerHandshakeHandler extends SimpleChannelInboundHandler<Handshake> {

	private ServerHandshaker serverHandshaker;

	public ServerHandshakeHandler(ServerHandshaker serverHandshaker) {
		this.serverHandshaker = serverHandshaker;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		Map<String, Object> out = Maps.newHashMap();
		serverHandshaker.prepare(ctx.channel(), out);
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
			serverHandshaker.handshake(ctx.channel(), msg.getAttributes(), out);
			Handshake handshake = new Handshake();
			handshake.setStep(3);
			handshake.setAttributes(out);
			ctx.writeAndFlush(handshake);
		}
	}

}
