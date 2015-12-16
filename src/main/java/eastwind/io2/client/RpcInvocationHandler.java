package eastwind.io2.client;

import io.netty.channel.ChannelPromise;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import eastwind.io.common.Host;
import eastwind.io2.EastwindUtils;
import eastwind.io2.Request;
import eastwind.io2.RequestHeader;
import eastwind.io2.TimeSequence10;

public class RpcInvocationHandler implements InvocationHandler {

	private String app;
	private RpcContextPool rpcContextPool;
	private TimeSequence10 timeSequence10;
	private ClientChannelManager clientChannelManager;

	public RpcInvocationHandler(String app, RpcContextPool rpcContextPool, TimeSequence10 timeSequence10,
			ClientChannelManager clientChannelManager) {
		this.app = app;
		this.rpcContextPool = rpcContextPool;
		this.timeSequence10 = timeSequence10;
		this.clientChannelManager = clientChannelManager;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		RpcContext rpcContext = RpcContext.LOCAL.get();
		if (rpcContext == null) {
			rpcContext = new RpcContext();
		}

		RequestHeader requestHeader = new RequestHeader();
		requestHeader.setId(timeSequence10.newId());
		requestHeader.setModel(RequestHeader.RPC);
		requestHeader.setNamespace(EastwindUtils.getDefaultNamespace(method));

		Request request = new Request();
		request.setHeader(requestHeader);
		request.setArg(args);

		ClientChannel clientChannel = null;
		Host host = rpcContext.getHost();
		if (host == null) {
			clientChannel = clientChannelManager.getActiveClientChannel(app);
		} else {
			clientChannel = clientChannelManager.getClientChannel(app, host);
		}

		ChannelPromise hsp = null;
		if (clientChannel == null || (hsp = clientChannel.getHandshakePromise()) == null || hsp.isCancelled()
				|| hsp.cause() != null) {
			// TODO fail
		}

		rpcContext.setRequest(request);
		rpcContextPool.put(rpcContext);
		hsp.channel().writeAndFlush(request);

		if (rpcContext.isSync()) {
			return rpcContext.sync();
		}

		// boolean, byte, char, short, int, long, float, double
		return method.getReturnType().isPrimitive() ? 0 : null;
	}

}
