package eastwind.io2;

import io.netty.channel.ChannelHandlerContext;

public class ConnectorInboundHandler extends TransportInboundHandler {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof NetworkTraffic) {
			NetworkTraffic networkTraffic = (NetworkTraffic) msg;
			NetworkTrafficTransport transport = (NetworkTrafficTransport) getTransport(ctx.channel());
			transport.push(networkTraffic);
		}
	}

}
