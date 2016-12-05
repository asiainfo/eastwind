package eastwind.io;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.ThreadPoolExecutor;

import eastwind.io.model.BusinessObject;
import eastwind.io.model.Request;
import eastwind.io.model.Response;

@Sharable
public class ServerBusinessHandler extends SimpleChannelInboundHandler<BusinessObject> {

	private ThreadPoolExecutor executor;
	private ServerContext providerContainer;
	
	public ServerBusinessHandler(ThreadPoolExecutor executor, ServerContext providerContainer) {
		this.executor = executor;
		this.providerContainer = providerContainer;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, BusinessObject msg) throws Exception {
		ProviderRegistry handlerRegistry = providerContainer.getProviderRegistry();
		Channel channel = ctx.channel();
		if (msg instanceof Request) {
			Request request = (Request) msg;
			MethodHandler methodHandler = handlerRegistry.findHandler(request.getName());
			Object result = methodHandler.invoke(request.getArgs());
			Response response = new Response();
			response.setId(request.getId());
			response.setBinary(request.isBinary());
			response.setResult(result);
			channel.writeAndFlush(response);
		}
	}

}
