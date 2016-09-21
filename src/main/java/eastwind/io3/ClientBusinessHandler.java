package eastwind.io3;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;

import java.util.concurrent.ThreadPoolExecutor;

import eastwind.io3.obj.BusinessObject;
import eastwind.io3.obj.Response;

@Sharable
public class ClientBusinessHandler extends SimpleChannelInboundHandler<BusinessObject> {

	private TransmitSustainer transmitSustainer;
	private ThreadPoolExecutor executor;
	
	public ClientBusinessHandler(TransmitSustainer transmitSustainer, ThreadPoolExecutor executor) {
		this.transmitSustainer = transmitSustainer;
		this.executor = executor;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, BusinessObject msg) throws Exception {
		if (msg instanceof Response) {
			Response response = (Response) msg;
			TransmitPromise promise = transmitSustainer.remove(response.getId());
			if (promise != null) {
				promise.set(response.getResult());
			}
		}
	}

}
