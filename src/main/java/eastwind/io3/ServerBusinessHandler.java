package eastwind.io3;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;

import java.util.concurrent.ThreadPoolExecutor;

import eastwind.io3.model.BusinessObject;
import eastwind.io3.model.Request;
import eastwind.io3.model.Response;

@Sharable
public class ServerBusinessHandler extends SimpleChannelInboundHandler<BusinessObject> {

	private ThreadPoolExecutor executor;
	private HandlerRegistry handlerRegistry;
	
	public ServerBusinessHandler(ThreadPoolExecutor executor, HandlerRegistry handlerRegistry) {
		this.executor = executor;
		this.handlerRegistry = handlerRegistry;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, BusinessObject msg) throws Exception {
		Channel channel = ctx.channel();
		if (msg instanceof Request) {
			Request request = (Request) msg;
			MethodHandler methodHandler = handlerRegistry.findHandler(request.getName());
			Object result = methodHandler.invoke(request.getArgs());
			Response response = new Response();
			response.setId(request.getId());
			response.setResult(result);
			channel.writeAndFlush(response);
		}
	}

}
