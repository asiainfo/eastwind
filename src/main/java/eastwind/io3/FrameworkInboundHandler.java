package eastwind.io3;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import eastwind.io.common.Ping;

@Sharable
public class FrameworkInboundHandler extends SimpleChannelInboundHandler<FrameworkObject> {
	
	private boolean server;
	private Application application;
	private ApplicationManager applicationManager;

	public FrameworkInboundHandler(boolean server, Application application,
			ApplicationManager applicationManager) {
		this.server = server;
		this.application = application;
		this.applicationManager = applicationManager;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		if (server) {
			ctx.channel().attr(ChannelAttr.SERVER).set(Boolean.TRUE);
		}
		super.channelActive(ctx);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FrameworkObject message) throws Exception {
		Channel channel = ctx.channel();
		if (message instanceof Handshake) {
			Handshake hs = (Handshake) message;
			TransportableApplicationGroup g = applicationManager.getOrCreate(hs.getGroup());
			if (Boolean.TRUE.equals(channel.attr(ChannelAttr.SERVER).get())) {
				TransportableApplication ta = g.getOrCreate(hs.getMyUuid());
				channel.attr(ChannelAttr.APPLICATION).set(ta);
				Transport transport = new Transport();
				transport.setChannel(channel);
				ta.setInboundTransport(transport);
				
				Handshake back = new Handshake();
				back.setGroup(application.getGroup());
				back.setMyUuid(application.getUuid());
				ctx.writeAndFlush(back);
			} else {
				TransportableApplication ta = channel.attr(ChannelAttr.APPLICATION).get();
				ta.setUuid(hs.getMyUuid());
				ta.getOutboundTransport().getTransportPromise().succeeded();
			}
		} else if (message instanceof Ping) {
			channel.writeAndFlush(message);
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
