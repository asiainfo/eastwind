package eastwind.io2.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import eastwind.io.common.Host;
import eastwind.io2.EastwindUtils;
import eastwind.io2.InternalRequest;
import eastwind.io2.Request;
import eastwind.io2.RequestHeader;
import eastwind.io2.TimeSequence10;

public class RpcInvocationHandler implements InvocationHandler {

	private String app;
	private RpcContextPool rpcContextPool;
	private TimeSequence10 timeSequence10;
	private OutboundChannelManager outboundChannelManager;

	public RpcInvocationHandler(String app, RpcContextPool rpcContextPool, TimeSequence10 timeSequence10,
			OutboundChannelManager outboundChannelManager) {
		this.app = app;
		this.rpcContextPool = rpcContextPool;
		this.timeSequence10 = timeSequence10;
		this.outboundChannelManager = outboundChannelManager;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Rpc rpc = Rpc.LOCAL.get();
		if (rpc == null) {
			rpc = new Rpc();
			rpc.setHost(new Host("127.0.0.1", 12468));
		}

		RequestHeader requestHeader = new RequestHeader();
		requestHeader.setId(timeSequence10.newId());
		requestHeader.setModel(RequestHeader.RPC);
		requestHeader.setNamespace(EastwindUtils.getDefaultNamespace(method));

		Request request = new Request();
		request.setHeader(requestHeader);
		request.setArg(args);
		rpc.setRequest(request);

		Host host = rpc.getHost();
		if (host == null) {
			return callByApp(method, rpc, request, app);
		} else {
			return callByHost(method, rpc, host);
		}
	}

	@SuppressWarnings("rawtypes")
	private Object callByHost(Method method, Rpc rpc, Host host) throws Throwable {
		RpcMediacy rpcMediacy = new RpcMediacy(rpc);
		if (rpc.isSync()) {
			for (int i = 0; i < 2; i++) {
				OutboundChannel oc = outboundChannelManager.getOutboundChannel(app, host);
				if (!oc.getHandshakePromise().isDone()) {
					oc.getHandshakePromise().sync();
				}
				if (oc.isActive()) {
					Channel channel = oc.getHandshakePromise().channel();
					if (i == 0) {
						oc.addRpcMediacy(rpcMediacy);
						ChannelFuture cf = channel.writeAndFlush(rpc.getRequest()).sync();
						if (cf.isSuccess()) {
							rpcMediacy.sync(10000);
						}
					} else if (i == 1) {
						Request r = new Request();
						RequestHeader header = new RequestHeader();
						header.setNamespace(InternalRequest.REC);
						r.setArg(rpc.getId());
						channel.writeAndFlush(r);
						Rpc rec = new Rpc<Integer>();
						rec.setRequest(r);
						rec.sync();
					}
				}
			}
		}
		return rpcMediacy.getResponse().getResult();
		// return returnNull(method);
	}

	@SuppressWarnings("rawtypes")
	private Object callByApp(Method method, Rpc rpcContext, Request request, String app) {

		return returnNull(method);
	}

	private Object returnNull(Method method) {
		// boolean, byte, char, short, int, long, float, double
		return method.getReturnType().isPrimitive() ? 0 : null;
	}

	static class FlushListener implements GenericFutureListener<ChannelFuture> {

		RpcMediacy rpcMediacy;

		public FlushListener(RpcMediacy rpcMediacy) {
			this.rpcMediacy = rpcMediacy;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (future.isSuccess()) {
				rpcMediacy.setFlushed();
			} else {
				rpcMediacy.setTh(future.cause());
			}
		}

	}
}
