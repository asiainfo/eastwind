package eastwind.io2.server;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import eastwind.io2.Handshake;

public class ServerHandshakeHandler extends ChannelDuplexHandler {

	private int state = 0;
	private ServerContext serverContext = new ServerContext();
	private ServerHandshake serverHandshake;

	public ServerHandshakeHandler() {
		this.serverHandshake = DefaultServerHandshake.INSTANCE;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (this.state == -1) {
			// handshake fail
			return;
		}
		if (msg instanceof Handshake) {
			Handshake handshakeIn = (Handshake) msg;
			if (this.state == 0) {
				HandshakeResult r = serverHandshake.handshake(handshakeIn.getApp(), handshakeIn.getHost(),
						handshakeIn.getProperties());
				boolean success = r == null || r.isSuccess();
				this.state = success ? 1 : -1;

				Handshake handshakeOut = new Handshake();
				handshakeOut.setUuid(serverContext.getUuid());
				handshakeOut.setApp(serverContext.getName());
				handshakeOut.setHost(serverContext.getHost());
				handshakeOut.setSuccess(success);

				ctx.pipeline().channel().writeAndFlush(handshakeOut);
			} else {
				// wrong data
			}
		} else if (this.state == 1) {
			super.channelRead(ctx, msg);
		}
	}

}
