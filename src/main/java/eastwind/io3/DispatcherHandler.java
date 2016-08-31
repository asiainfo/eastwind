package eastwind.io3;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eastwind.io3.obj.FrameworkObject;

@Sharable
public class DispatcherHandler extends ChannelInboundHandlerAdapter {

	private static Logger logger = LoggerFactory.getLogger(DispatcherHandler.class);

	private boolean server;
	private FrameworkObjectHandler frameworkObjectHandler;

	public DispatcherHandler(boolean server, FrameworkObjectHandler frameworkObjectHandler) {
		this.server = server;
		this.frameworkObjectHandler = frameworkObjectHandler;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof FrameworkObject) {
			if (server) {
				frameworkObjectHandler.handleObjectFromClient(msg, ctx.channel());
			} else {
				frameworkObjectHandler.handleObjectFromServer(msg, ctx.channel());
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
	}

}
