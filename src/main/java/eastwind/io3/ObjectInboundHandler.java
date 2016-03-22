package eastwind.io3;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

@Sharable
public class ObjectInboundHandler extends SimpleChannelInboundHandler<Object> {

	private ObjectHandlerRegistry objectHandlerRegistry;
	private ApplicationManager applicationManager;
	
	public ObjectInboundHandler(ObjectHandlerRegistry objectHandlerRegistry, ApplicationManager applicationManager) {
		this.objectHandlerRegistry = objectHandlerRegistry;
		this.applicationManager = applicationManager;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object message) throws Exception {
		if (message instanceof Request) {
			Request request = (Request) message;
			RpcHandler handler = objectHandlerRegistry.getRpcHandler(request.getNamespace());
			Object result = handler.invoke(request.getArgs());
			Response response = new Response();
			response.setId(request.getId());
			response.setResult(result);
			ctx.writeAndFlush(response);
		} else if (message instanceof Response){
			Response response = (Response) message;
			TransportableApplication ta = ctx.channel().attr(ChannelAttr.APPLICATION).get();
			RpcPromise rpcPromise = ta.remove(response.getId());
			if (rpcPromise != null) {
				rpcPromise.succeeded(response);
			}
		} else {
			TransportableApplication ta = ctx.channel().attr(ChannelAttr.APPLICATION).get();
			List<MessageListener<Object>> listeners = objectHandlerRegistry.getMessageListeners(message.getClass());
			for (MessageListener listener : listeners) {
				listener.onMessage(message, ta.getTransport(ctx.channel()));
			}
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
