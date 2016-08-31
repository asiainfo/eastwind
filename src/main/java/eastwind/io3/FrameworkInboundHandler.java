package eastwind.io3;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import eastwind.io3.obj.FrameworkObject;

@Sharable
public class FrameworkInboundHandler extends SimpleChannelInboundHandler<FrameworkObject> {

	private boolean server;
	private TransmitSustainer transmitSustainer;
	private ObjectHandlerRegistry objectHandlerRegistry;

	public FrameworkInboundHandler(boolean server, TransmitSustainer transmitSustainer,
			ObjectHandlerRegistry objectHandlerRegistry) {
		this.server = server;
		this.transmitSustainer = transmitSustainer;
		this.objectHandlerRegistry = objectHandlerRegistry;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		if (server) {
//			ctx.channel().attr(ChannelAttr.SERVER).set(Boolean.TRUE);
		}
		super.channelActive(ctx);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FrameworkObject message) throws Exception {
//		if (message instanceof UniqueObject) {
//			UniqueObject uo = (UniqueObject) message;
//			if (uo.isCall()) {
//				handleCall(ctx, uo);
//			} else {
//			}
//		}

		// if (message instanceof RpcDescriptor) {
		// RpcDescriptor rd = (RpcDescriptor) message;
		// objectHandlerRegistry.getRpcHandler(rd.getInterf(), rd.getMethod(),
		// rd.getParameterTypes());
		// } else if (message instanceof Handshake) {
		// Handshake hs = (Handshake) message;
		// RemoteApplicationGroup g =
		// applicationManager.getOrCreate(hs.getGroup());
		// if (Boolean.TRUE.equals(channel.attr(ChannelAttr.SERVER).get())) {
		// RemoteApplication ta = g.getOrCreate(hs.getMyUuid());
		// channel.attr(ChannelAttr.APPLICATION).set(ta);
		// Transport transport = new Transport();
		// transport.setChannel(channel);
		// ta.setInboundTransport(transport);
		//
		// Handshake back = new Handshake();
		// back.setGroup(application.getGroup());
		// back.setMyUuid(application.getUuid());
		// ctx.writeAndFlush(back);
		// } else {
		// RemoteApplication ta = channel.attr(ChannelAttr.APPLICATION).get();
		// ta.setUuid(hs.getMyUuid());
		// ta.getOutboundTransport().getTransportPromise().succeeded();
		// }
		// } else if (message instanceof Ping) {
		// channel.writeAndFlush(message);
		// }
	}

//	private void handleCall(ChannelHandlerContext ctx, UniqueObject uo) throws ClassNotFoundException {
//		Object o = uo.getObj();
//		Channel channel = ctx.channel();
//		if (o instanceof Handling) {
//			Handling handling = (Handling) o;
//			if (ra.getMessage(handling.getId()) != null) {
//				writeBack(ctx, uo, 1);
//			} else {
//				writeBack(ctx, uo, 0);
//			}
//		} else if (o instanceof HandlerEnquire) {
//			HandlerEnquire hd = (HandlerEnquire) o;
//			RpcHandler handler = objectHandlerRegistry.getHandler(hd.getInterf(), hd.getMethod(),
//					hd.getParameterTypes());
//			HandlerMetaData meta = new HandlerMetaData();
//			meta.setName(handler.getAlias());
//			writeBack(ctx, uo, meta);
//		} else if (o instanceof Shake) {
//			Shake hs = (Shake) o;
//			Shake hs2 = applicationManager.access(hs, channel);
//			writeBack(ctx, uo, hs2);
//		}
//	}

//	private void writeBack(ChannelHandlerContext ctx, UniqueObject uo, Object result) {
//		UniqueObject back = new UniqueObject(result).call(false);
//		back.setId(uo.getId());
//		ctx.writeAndFlush(back);
//	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (!ctx.channel().isActive()) {
			return;
		}
		super.exceptionCaught(ctx, cause);
	}
}
