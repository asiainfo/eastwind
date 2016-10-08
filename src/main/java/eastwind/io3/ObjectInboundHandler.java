package eastwind.io3;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

import eastwind.io3.model.HandlingMessage;
import eastwind.io3.model.Request;
import eastwind.io3.model.Response;

@Sharable
public class ObjectInboundHandler extends SimpleChannelInboundHandler<Object> {

	private ObjectHandlerRegistry objectHandlerRegistry;
	private TransmitSustainer transmitSustainer;
	private ThreadPoolExecutor executor;

	public ObjectInboundHandler(ObjectHandlerRegistry objectHandlerRegistry, TransmitSustainer transmitSustainer,
			ThreadPoolExecutor executor) {
		this.objectHandlerRegistry = objectHandlerRegistry;
		this.transmitSustainer = transmitSustainer;
		this.executor = executor;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void channelRead0(final ChannelHandlerContext ctx, Object message) throws Exception {
		if (message instanceof Request) {
			final Request request = (Request) message;
			final MethodHandler handler = objectHandlerRegistry.getHandler(request.getName());

			final HandlingMessage hm = new HandlingMessage();
			hm.setId(request.getId());

			executor.submit(new Runnable() {
				@Override
				public void run() {
					try {
						hm.setResponse(handler.invoke(request.getArgs()));
						Response response = new Response();
						response.setId(request.getId());
						response.setResult(hm.getResponse());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		} else if (message instanceof Response) {
			Response response = (Response) message;
		}
	}

	static class CleanListener implements GenericFutureListener<ChannelFuture> {

		HandlingMessage message;

		public CleanListener(HandlingMessage message) {
			this.message = message;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
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
