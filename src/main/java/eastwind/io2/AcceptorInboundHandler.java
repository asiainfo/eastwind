package eastwind.io2;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Sharable
public class AcceptorInboundHandler extends TransportInboundHandler {

	protected static Logger logger = LoggerFactory.getLogger(EndPoint.class);
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		logger.debug("handler added to {}", ctx.channel());
		InboundTransport transport = new InboundTransport(ctx.channel());
		setTransport(ctx.channel(), transport);
		super.handlerAdded(ctx);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof NetworkTraffic) {
			NetworkTraffic networkTraffic = (NetworkTraffic) msg;
			NetworkTrafficTransport transport = (NetworkTrafficTransport) getTransport(ctx.channel());
			transport.push(networkTraffic);
		}
	}

}
