package eastwind.io.nioclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPromise;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Maps;

import eastwind.io.ChannelAttr;
import eastwind.io.common.CommonUtils;
import eastwind.io.common.Host;
import eastwind.io.common.ScheduledExecutor;

public class ChannelGuard {

	private Bootstrap bootstrap;
	private int pingInterval = 3;

	private ConcurrentMap<Host, Long> shutdowns = Maps.newConcurrentMap();
	private ConcurrentMap<Host, ConnectStat> connectStats = Maps.newConcurrentMap();

	public ChannelGuard(Bootstrap bootstrap) {
		this.bootstrap = bootstrap;
	}

	public void add(String app, Host host, ClientHandshaker clientHandshaker) {
		ConnectStat connectStat = new ConnectStat(app, host, clientHandshaker);
		ConnectStat old = connectStats.put(host, connectStat);
		if (old != null) {
			old.setStoped(true);
		}
		connect(connectStat, false);
	}

	public ChannelFuture get(Host host) {
		return connectStats.get(host).getChannelFuture();
	}

	public void connectNow(Host host) {
		ConnectStat connectStat = connectStats.get(host);
		connect(connectStat, true);
	}

	private void connect(ConnectStat connectStat, boolean resetDelay) {
		synchronized (connectStat) {
			if (connectStat.getChannelFuture() != null
					&& (connectStat.getChannelFuture().channel().isActive() || connectStat.isConnecting())) {
				return;
			}
			if (resetDelay) {
				connectStat.resetDelay();
			}
			Host host = connectStat.getHost();
			connectStat.incrementMod();
			connectStat.setConnecting(true);
			ChannelFuture cf = bootstrap.connect(host.getIp(), host.getPort());
			connectStat.setChannelFuture(cf);

			ChannelAttr.set(cf.channel(), ChannelAttr.APP, connectStat.getApp());
			ChannelAttr.set(cf.channel(), ChannelAttr.CLIENT_HANDSHAKE, connectStat.getClientHandshaker());
			ChannelAttr.set(cf.channel(), ChannelAttr.HOST, connectStat.getHost());
			ChannelPromise handshakePromise = ChannelAttr.initHandshakePromise(cf.channel());
			connectStat.setChannelFuture(handshakePromise);

			cf.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					ChannelPromise handshakePromise = ChannelAttr.get(future.channel(), ChannelAttr.HANDSHAKE_PROMISE);
					if (future.isCancelled()) {
						handshakePromise.cancel(true);
					}
					if (!future.isSuccess()) {
						handshakePromise.setFailure(future.cause());
					}
				}
			});

			cf.addListener(new ReconnectListener(connectStat));
		}
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

	private class ReconnectListener implements ChannelFutureListener {

		private int mod;
		private ConnectStat connectStat;

		public ReconnectListener(ConnectStat connectStat) {
			this.connectStat = connectStat;
			this.mod = connectStat.getMode();
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			connectStat.setConnecting(false);
			if (future.isSuccess()) {
				connectStat.resetDelay();
				// reconnect after close
				future.channel().closeFuture().addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						if (mod == connectStat.getMode()) {
							ScheduledExecutor.ses.schedule(new Runnable() {
								@Override
								public void run() {
									connect(connectStat, false);
								}
							}, connectStat.getDelay(), TimeUnit.MILLISECONDS);
						}
					}
				});
			}

			if (future.cause() != null) {
				// TODO connect cause or handshake cause
				System.out.println("connect fail:" + connectStat.getHost());
				synchronized (connectStat) {
					if (mod == connectStat.getMode()) {
						ScheduledExecutor.ses.schedule(new Runnable() {
							@Override
							public void run() {
								connect(connectStat, false);
							}
						}, connectStat.getDelay(), TimeUnit.MILLISECONDS);
					}
				}
			}
		}

	}

	private class PingRunner implements Runnable {
		@Override
		public void run() {
			// TODO ping同时并检测失效连接是否在重连
			// TODO 断网判断与恢复
			long now = CommonUtils.currentTimeSeconds();

			// for (Entry<Host, ChannelFuture> en : channels.entrySet()) {
			//
			// ChannelFuture cf = en.getValue();
			// Channel channel = cf.channel();
			//
			// if (cf.isSuccess() && channel.isActive()) {
			// Long t = ChannelAttr.get(channel,
			// ChannelAttr.SESSION).getLastAccessedTime();
			// if (t != null && now - t.longValue() > pingInterval) {
			// channel.writeAndFlush(Ping.instance);
			// }
			// }
			//
			// }
		}
	}

}
