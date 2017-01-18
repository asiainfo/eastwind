package eastwind.io2;

import io.netty.channel.Channel;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

public abstract class TransportInboundHandler extends SimpleChannelInboundHandler<Object> {

	public static final AttributeKey<Transport> TRANSPORT = AttributeKey.valueOf("TRANSPORT");
	
	public static Transport getTransport(Channel channel) {
		return channel.attr(TRANSPORT).get();
	}
	
	public static void setTransport(Channel channel, Transport transport) {
		channel.attr(TRANSPORT).set(transport);
	}
}
