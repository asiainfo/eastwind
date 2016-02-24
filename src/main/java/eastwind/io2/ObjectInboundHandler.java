package eastwind.io2;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

import eastwind.io2.client.OutboundChannel;
import eastwind.io2.client.RpcContextPool;
import eastwind.io2.client.RpcMediacy;

public class ObjectInboundHandler extends SimpleChannelInboundHandler<Object> {

	private RpcContextPool rpcContextPool;
	private ObjectHandlerRegistry objectHandlerRegistry;

	public ObjectInboundHandler(RpcContextPool rpcContextPool, ObjectHandlerRegistry objectHandlerRegistry) {
		this.rpcContextPool = rpcContextPool;
		this.objectHandlerRegistry = objectHandlerRegistry;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object message) throws Exception {
		if (message instanceof Request) {
			Request request = (Request) message;
			RequestHeader header = request.getHeader();
			if (RequestHeader.isMessage(header)) {
				List<MessageListener<Object>> listeners = objectHandlerRegistry.getMessageListeners(request.getArg()
						.getClass());
				for (MessageListener<Object> listener : listeners) {
					listener.onMessage(request.getArg());
				}
			} else if (RequestHeader.isRpc(header)) {
				RpcHandler handler = objectHandlerRegistry.getRpcHandler(header.getNamespace(),
						header.getParamLens().length);
				Object result = handler.invoke((Object[]) request.getArg());

				ResponseHeader responseHeader = new ResponseHeader();
				responseHeader.setId(header.getId());
				Response response = new Response();
				response.setHeader(responseHeader);
				response.setResult(result);
				ctx.writeAndFlush(response);
			}
		} else if (message instanceof Response) {
			Response response = (Response) message;
			OutboundChannel cc = OutboundChannel.get(ctx.channel());
			RpcMediacy rpcMediacy = cc.removeRpcMediacy(response.getHeader().getId());
			rpcMediacy.setResponse(response);
//			if (rpcContextPool != null) {
//				Response response = (Response) message;
//				@SuppressWarnings("rawtypes")
//				Rpc rpcContext = rpcContextPool.remove(response.getHeader().getId());
//				rpcContext.done(response);
//			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (cause.getMessage().contains("远程主机强迫关闭了一个现有的连接")) {
			return;
		}
		super.exceptionCaught(ctx, cause);
	}
}
