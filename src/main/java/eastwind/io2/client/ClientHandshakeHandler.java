package eastwind.io2.client;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import eastwind.io2.Handshake;

public class ClientHandshakeHandler extends ChannelDuplexHandler {

	private int state;
	private ClientContext clientContext = new ClientContext();

	// public ClientHandshakeHandler(ClientContext clientContext) {
	// this.clientContext = clientContext;
	// }

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ClientChannel cc = ClientChannel.get(ctx.channel());
		cc.addConnectedTimes();
		cc.setLastConnectedTime(System.currentTimeMillis());

		ClientHandshake clientHandshake = getHandshake(ctx);
		Handshake handshake = new Handshake();
		handshake.setSuccess(true);
		handshake.setApp(clientContext.getApp());
		handshake.setUuid(clientContext.getUuid());
		handshake.setProperties(clientHandshake.prepare());
		ctx.channel().writeAndFlush(handshake);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ClientChannel cc = ClientChannel.get(ctx.channel());
		cc.setLastCloseTime(System.currentTimeMillis());
		cc.setHandshakePromise(null);
		super.channelInactive(ctx);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		if (state == 1 && !(msg instanceof Handshake)) {
			super.write(ctx, msg, promise);
		} else if (state == 0 && msg instanceof Handshake) {
			super.write(ctx, msg, promise);
		} else {
			// TODO XXXX
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof Handshake) {
			Handshake handshake = (Handshake) msg;
			ClientChannel clientChannel = ClientChannel.get(ctx.channel());
			if (handshake.isSuccess()) {
				this.state = 1;
				clientChannel.getHandshakePromise().setSuccess();
			} else {
				this.state = -1;
			}
		} else {
			super.channelRead(ctx, msg);
		}
	}

	private ClientHandshake getHandshake(ChannelHandlerContext ctx) {
		ClientChannel cc = ClientChannel.get(ctx.channel());
		ClientHandshake clientHandshake = cc.getClientHandshake();
		if (clientHandshake == null) {
			clientHandshake = DefaultClientHandshake.INSTANCE;
		}
		return clientHandshake;
	}

}
