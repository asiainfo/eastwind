package eastwind.io3;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

@Sharable
public class ObjectInboundHandler extends SimpleChannelInboundHandler<Object> {

	private RemoteAppManager applicationManager;
	private ObjectHandlerRegistry objectHandlerRegistry;
	private PromiseSustainer promiseSustainer;
	private ThreadPoolExecutor executor;

	public ObjectInboundHandler(RemoteAppManager applicationManager, ObjectHandlerRegistry objectHandlerRegistry,
			PromiseSustainer promiseSustainer, ThreadPoolExecutor executor) {
		this.applicationManager = applicationManager;
		this.objectHandlerRegistry = objectHandlerRegistry;
		this.promiseSustainer = promiseSustainer;
		this.executor = executor;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void channelRead0(final ChannelHandlerContext ctx, Object message) throws Exception {
		if (message instanceof Request) {
			final Request request = (Request) message;
			final RpcHandler handler = objectHandlerRegistry.getHandler(request.getNamespace());

			final HandlingMessage hm = new HandlingMessage();
			hm.setId(request.getId());
			final RemoteApp app = applicationManager.getTransport(ctx.channel()).getRemoteApplication();
			app.addMessage(hm);
			
			executor.submit(new Runnable() {
				@Override
				public void run() {
					try {
						hm.setResponse(handler.invoke(request.getArgs()));
						Response response = new Response();
						response.setId(request.getId());
						response.setResult(hm.getResponse());
						ctx.writeAndFlush(response).addListener(new CleanListener(app, hm));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		} else if (message instanceof Response) {
			Response response = (Response) message;
			ListenablePromise lp = promiseSustainer.remove(response.getId());
			lp.succeeded(response.getResult());
		}
	}

	static class CleanListener implements GenericFutureListener<ChannelFuture> {

		RemoteApp app;
		HandlingMessage message;
		
		public CleanListener(RemoteApp app, HandlingMessage message) {
			this.app = app;
			this.message = message;
		}
		
		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			app.removeMessage(message.getId());
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