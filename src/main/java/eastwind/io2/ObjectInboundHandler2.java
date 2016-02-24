package eastwind.io2;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

public class ObjectInboundHandler2 extends SimpleChannelInboundHandler<Object> {

	private ObjectHandlerRegistry objectHandlerRegistry;

	public ObjectInboundHandler2(ObjectHandlerRegistry objectHandlerRegistry) {
		this.objectHandlerRegistry = objectHandlerRegistry;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object message) throws Exception {
		if (message instanceof HeadedObject) {
			
		} else {
			List<MessageListener<Object>> listeners = objectHandlerRegistry.getMessageListeners(message.getClass());
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (!ctx.channel().isActive()) {
			return;
		}
		super.exceptionCaught(ctx, cause);
	}
}
