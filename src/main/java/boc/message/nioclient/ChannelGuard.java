package boc.message.nioclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPromise;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import boc.message.ChannelAttr;
import boc.message.common.CommonUtils;
import boc.message.common.Host;
import boc.message.common.Ping;
import boc.message.common.SharedScheduledExecutor;

import com.google.common.collect.Maps;

public class ChannelGuard {

	private Bootstrap bootstrap;

	private int pingInterval;

	private ConcurrentMap<Host, Long> shutdowns = Maps.newConcurrentMap();
	private ConcurrentMap<Host, ChannelFuture> channels = Maps.newConcurrentMap();

	public ChannelGuard(Bootstrap bootstrap, int timeout) {
		this.bootstrap = bootstrap;
		this.pingInterval = timeout / 3;
	}

	public void start() {
		// SharedScheduledExecutor.ses.scheduleWithFixedDelay(new PingRunner(),
		// this.pingInterval, this.pingInterval,
		// TimeUnit.SECONDS);
	}

	public void shutdown(Host host) {
		shutdowns.put(host, System.currentTimeMillis());
	}

	public boolean isShutdowning(Host host) {
		Long t = shutdowns.get(host);
		if (t == null) {
			return false;
		} else {
			if (System.currentTimeMillis() - t.longValue() < 30000) {
				return true;
			} else {
				shutdowns.remove(host);
			}
			return false;
		}
	}

	public ChannelFuture getOrConnect(String app, Host host) {
		ChannelFuture cf = channels.get(host);
		if (cf != null) {
			return cf;
		}
		connect(app, host);
		return channels.get(host);
	}

	private synchronized ChannelFuture connect(final String app, final Host host) {
		ChannelFuture cf = channels.get(host);
		if (cf != null) {
			return cf;
		}

		cf = bootstrap.connect(host.getIp(), host.getPort());
		ChannelAttr.set(cf.channel(), ChannelAttr.APP, app);
		final ChannelPromise cp = ChannelAttr.initHandShakePromise(cf.channel());

		cf.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isCancelled()) {
					cp.cancel(true);
				}
				if (!future.isSuccess()) {
					cp.setFailure(future.cause());
				}
			}
		});

		cf.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					// reconnect after close
					future.channel().closeFuture().addListener(new ChannelFutureListener() {
						@Override
						public void operationComplete(ChannelFuture future) throws Exception {
							System.out.println("closed");
							SharedScheduledExecutor.ses.schedule(new Runnable() {
								@Override
								public void run() {
									connect(app, host);
								}
							}, 1, TimeUnit.SECONDS);
						}
					});
				}

				if (future.cause() != null) {
					// reconnect if connect fail
					SharedScheduledExecutor.ses.schedule(new Runnable() {
						@Override
						public void run() {
							connect(app, host);
						}
					}, 10, TimeUnit.SECONDS);
				}
			}
		});

		channels.put(host, cp);
		return cp;
	}

	private class PingRunner implements Runnable {
		@Override
		public void run() {
			long now = CommonUtils.currentTimeSeconds();

			for (Entry<Host, ChannelFuture> en : channels.entrySet()) {

				ChannelFuture cf = en.getValue();
				Channel channel = cf.channel();

				if (cf.isSuccess() && channel.isActive()) {
					Long t = ChannelAttr.get(channel, ChannelAttr.SESSION).getLastAccessedTime();
					if (t != null && now - t.longValue() > pingInterval) {
						channel.writeAndFlush(Ping.instance);
					}
				}

			}
		}
	}

}
