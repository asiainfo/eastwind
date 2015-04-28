package eastwind.io.nioclient;

import io.netty.channel.ChannelFuture;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

import eastwind.io.common.Host;
import eastwind.io.common.InvocationFuture;
import eastwind.io.common.InvocationFuturePool;
import eastwind.io.common.Request;
import eastwind.io.common.TimedIdSequence100;

public class InvocationFutureHandler implements InvocationHandler {

	private TimedIdSequence100 timedIdSequence100;
	private ProviderGroup providerGroup;
	private ChannelGuard channelGuard;
	private InvocationFuturePool invocationFuturePool;

	public InvocationFutureHandler(TimedIdSequence100 timedIdSequence100, ProviderGroup providerGroup,
			ChannelGuard channelGuard, InvocationFuturePool invocationFuturePool) {
		this.timedIdSequence100 = timedIdSequence100;
		this.providerGroup = providerGroup;
		this.channelGuard = channelGuard;
		this.invocationFuturePool = invocationFuturePool;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (Object.class.equals(method.getDeclaringClass())) {
			return method.invoke(this, args);
		}
		InvocationFuture invocationFuture = InvocationFuture.INVOCATION_FUTURE_LOCAL.get();
		if (invocationFuture == null) {
			invocationFuture = new InvocationFuture();
			invocationFuture.setSync(true);
		}

		if (invocationFuture.isBroadcast()) {
			List<Host> hosts = providerGroup.getHosts();
			for (Host host : hosts) {
				InvocationFuture sub = new InvocationFuture();
				setUpInvocationFuture(sub, host, method, args);
				invocationFuture.addSubInvocationFuture(sub);
			}

			List<InvocationFuture> subs = invocationFuture.getSubInvocationFutures();
			for (InvocationFuture sub : subs) {
				send(sub);
			}

			if (invocationFuture.isSync()) {
				for (InvocationFuture sub : subs) {
					try {
						sub.sync();
					} catch (Exception e) {
					}
				}
			}
			InvocationFuture.INVOCATION_FUTURE_LOCAL.set(null);
			return null;
		} else {
			Host host = invocationFuture.getHost();
			if (host == null) {
				host = providerGroup.nextHost();
				invocationFuture.setHost(host);
			} else if (!providerGroup.contain(host)) {
				// TODO illegal host
			}
			setUpInvocationFuture(invocationFuture, host, method, args);
			send(invocationFuture);

			InvocationFuture.INVOCATION_FUTURE_LOCAL.set(null);
			if (invocationFuture.isSync()) {
				return invocationFuture.sync();
			}
			// boolean, byte, char, short, int, long, float, double
			return method.getReturnType().isPrimitive() ? 0 : null;
		}
	}

	private void setUpInvocationFuture(InvocationFuture<?> invocationFuture, Host host, Method method, Object[] args) {
		Request request = new Request();
		request.setId(timedIdSequence100.newId());
		request.setType(method.getName());
		request.setArgs(args);

		invocationFuture.setApp(providerGroup.getApp());
		invocationFuture.setHost(host);
		invocationFuture.setRequest(request);
	}

	private void send(InvocationFuture<?> invocationFuture) {
		Host host = invocationFuture.getHost();
		ChannelFuture channelFuture = channelGuard.get(host);
		if (channelFuture.isSuccess()) {
			invocationFuturePool.put(invocationFuture);
			channelFuture.channel().writeAndFlush(invocationFuture.getRequest());
		} else if (channelFuture.cause() != null) {
			invocationFuture.fail();
		} else {
			channelFuture.addListener(new InvocationChannelListener(invocationFuture, invocationFuturePool));
		}
	}
}
