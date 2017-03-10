package eastwind.io2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

public class TransportInboundHandler extends SimpleChannelInboundHandler<Object> {

	public static final AttributeKey<Transport> TRANSPORT = AttributeKey.valueOf("TRANSPORT");
	protected static Logger logger = LoggerFactory.getLogger(TransportInboundHandler.class);
	
	public static Transport getTransport(Channel channel) {
		return channel.attr(TRANSPORT).get();
	}
	
	public static void setTransport(Channel channel, Transport transport) {
		channel.attr(TRANSPORT).set(transport);
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
