package eastwind.io3;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;

@Sharable
public class ObjectInboundHandler extends SimpleChannelInboundHandler<Object> {

	private ObjectHandlerRegistry objectHandlerRegistry;
	private ApplicationManager applicationManager;
	private TransportContext transportContext;

	public ObjectInboundHandler(ObjectHandlerRegistry objectHandlerRegistry, ApplicationManager applicationManager,
			TransportContext transportContext) {
		this.objectHandlerRegistry = objectHandlerRegistry;
		this.applicationManager = applicationManager;
		this.transportContext = transportContext;
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
		} else if (message instanceof Response) {
			Response response = (Response) message;
			ListenablePromise lp = transportContext.remove(response.getId());
			lp.succeeded(response.getResult());
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (cause.getClass().equals(IOException.class)) {
			return;
		}
		super.exceptionCaught(ctx, cause);
	}
}
