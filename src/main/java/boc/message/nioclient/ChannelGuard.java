package boc.message.nioclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import boc.message.ChannelStat;
import boc.message.common.Host;
import boc.message.common.SharedScheduledExecutor;

import com.google.common.collect.Maps;

public class ChannelGuard {

	private Bootstrap bootstrap;

	private CioClient cioClient;
	private int pingInterval;

	private ConcurrentMap<Host, Long> shutdowns = Maps.newConcurrentMap();
	private ConcurrentMap<Host, ChannelFuture> channels = Maps.newConcurrentMap();

	public ChannelGuard(Bootstrap bootstrap, CioClient cioClient) {
		this.bootstrap = bootstrap;
		this.cioClient = cioClient;
		this.pingInterval = this.cioClient.getChannelTimeout() / 3;
	}

	public void setHosts(List<Host> hosts) {
		for (Host host : hosts) {
			connect(host);
		}
	}

	public void start() {
		SharedScheduledExecutor.ses.scheduleWithFixedDelay(new PingRunner(), this.pingInterval, this.pingInterval,
				TimeUnit.SECONDS);
	}

	public void setShutdown(Host host) {
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

	public ChannelFuture getChannelOrConnect(Host host) {
		ChannelFuture cf = channels.get(host);
		if (cf != null) {
			return cf;
		}
		connect(host);
		return channels.get(host);
	}

	public Channel getChannel(Host host) {
		ChannelFuture cf = channels.get(host);
		if (cf == null) {
			return null;
		}
		return cf.channel();
	}

	private void connect(final Host host) {
		ChannelFuture cf = bootstrap.connect(host.getIp(), host.getPort());

		for (;;) {
			ChannelFuture oldCf = channels.putIfAbsent(host, cf);
			if (oldCf == null) {
				break;
			} else {
				if (oldCf.channel().isOpen()) {
					return;
				} else {
					synchronized (oldCf) {
						if (channels.get(host) == oldCf) {
							channels.put(host, cf);
							break;
						} else {
							return;
						}
					}
				}
			}
		}

		cf.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					ChannelStat.set(future.channel(), ChannelStat.lastWriteTime, System.currentTimeMillis() / 1000);

					// reconnect after close
					future.channel().closeFuture().addListener(new ChannelFutureListener() {
						@Override
						public void operationComplete(ChannelFuture future) throws Exception {
							System.out.println("closed");
							SharedScheduledExecutor.ses.schedule(new Runnable() {
								@Override
								public void run() {
									connect(host);
								}
							}, 1, TimeUnit.SECONDS);
						}
					});

				} else if (future.cause() != null) {

					// reconnect if connect fail
					SharedScheduledExecutor.ses.schedule(new Runnable() {
						@Override
						public void run() {
							connect(host);
						}
					}, 10, TimeUnit.SECONDS);

				}
			}
		});
	}

	private class PingRunner implements Runnable {
		@Override
		public void run() {
			long now = System.currentTimeMillis() / 1000;

			for (Entry<Host, ChannelFuture> en : channels.entrySet()) {

				ChannelFuture cf = en.getValue();
				Channel channel = cf.channel();

				if (cf.isSuccess() && channel.isActive()) {
					Long t = ChannelStat.get(channel, ChannelStat.lastWriteTime);
					
					if (t != null && now - t.longValue() > pingInterval) {
						InetSocketAddress host = (InetSocketAddress) channel.remoteAddress();
						String ip = host.getAddress().getHostAddress();
						int port = host.getPort();
						cioClient.ping(new Host(ip, port));
					}

				}

			}
		}
	}

}
