package eastwind.io2.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPromise;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eastwind.io.common.CommonUtils;
import eastwind.io.common.Host;
import eastwind.io2.Handshake;

public class OutboundChannelManager {

	private Bootstrap bootstrap;

	private ApplicationConfigManager applicationConfigManager;

	private ConcurrentMap<String, AppOutboundChannel> appClientChannels = Maps.newConcurrentMap();

	public OutboundChannelManager(Bootstrap bootstrap, ApplicationConfigManager applicationConfigManager) {
		this.bootstrap = bootstrap;
		this.applicationConfigManager = applicationConfigManager;
	}

	public OutboundChannel getActiveOutboundChannel(String app) {
		AppOutboundChannel appOutboundChannel = getAppOutboundChannel(app);
		OutboundChannel oc = appOutboundChannel.getActiveChannel();
		if (oc != null) {
			return oc;
		}
		List<OutboundChannel> channels = appOutboundChannel.channels;
		int size = channels.size();
		MultipleConnectListener listener = new MultipleConnectListener();
		for (int i = 0; i < size; i++) {
			oc = channels.get(i);
			connect(oc);
			ChannelPromise cp = oc.getHandshakePromise();
			if (cp != null) {
				cp.addListener(listener);
				listener.futures.add(cp);
			}
		}
		synchronized (listener) {
			if (listener.getState() == 1) {
				ChannelFuture cf = listener.getActiveCf();
				return OutboundChannel.get(cf.channel());
			} else {
				try {
					listener.wait(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (listener.getState() == 1) {
			ChannelFuture cf = listener.getActiveCf();
			return OutboundChannel.get(cf.channel());
		}
		// throw ex
		return null;
	}

	public OutboundChannel getOutboundChannel(String app, Host host) {
		AppOutboundChannel appOutboundChannel = getAppOutboundChannel(app);
		OutboundChannel oc = appOutboundChannel.getChannel(host);
		if (oc != null) {
			if (oc.getHandshakePromise() == null) {
				connect(oc);
			}
			return oc;
		}
		appOutboundChannel.writeLock.lock();
		try {
			oc = appOutboundChannel.getChannel(host);
			if (oc == null) {
				oc = new OutboundChannel(host);
				appOutboundChannel.addClientChannel(oc);
			}
		} finally {
			appOutboundChannel.writeLock.unlock();
		}
		connect(oc);
		return oc;
	}

	public ApplicationConfigManager getApplicationConfigManager() {
		return applicationConfigManager;
	}

	private AppOutboundChannel getAppOutboundChannel(String app) {
		AppOutboundChannel appOutboundChannel = this.appClientChannels.get(app);
		if (appOutboundChannel == null) {
			appOutboundChannel = new AppOutboundChannel();
			appOutboundChannel = CommonUtils.putIfAbsent(appClientChannels, app, appOutboundChannel);
		}
		return appOutboundChannel;
	}

	private void connect(OutboundChannel oc) {
		synchronized (oc) {
			if (oc.isActive()) {
				return;
			}
			ChannelPromise hp = oc.getHandshakePromise();
			if (hp != null && !hp.isDone()) {
				return;
			}
			
			Host host = oc.getHost();
			ChannelFuture cf = bootstrap.connect(host.getIp(), host.getPort());
			
			oc.addConnectedTimes();
			oc.setLastConnectedTime(System.currentTimeMillis());
			
			hp = cf.channel().newPromise();
			oc.setHandshakePromise(hp);
			OutboundChannel.set(cf.channel(), oc);

			ReconnectListener reconnectListener = oc.getReconnectListener();
			if (reconnectListener == null) {
				reconnectListener = new ReconnectListener(oc);
				oc.setReconnectListener(reconnectListener);
			}
			cf.addListener(new HandshakeListener(oc));
			cf.addListener(reconnectListener);
		}
	}

	private static class AppOutboundChannel {

		AtomicInteger cur = new AtomicInteger(0);
		List<OutboundChannel> channels = Lists.newArrayList();
		ReadWriteLock lock = new ReentrantReadWriteLock();
		Lock readLock = lock.readLock();
		Lock writeLock = lock.writeLock();

		void addClientChannel(OutboundChannel clientChannel) {
			this.channels.add(clientChannel);
		}

		OutboundChannel getChannel(Host host) {
			readLock.lock();
			try {
				for (OutboundChannel oc : channels) {
					if (oc != null && oc.getHost().equals(host)) {
						return oc;
					}
				}
				return null;
			} finally {
				readLock.unlock();
			}
		}

		OutboundChannel getActiveChannel() {
			try {
				readLock.lock();
				OutboundChannel clientChannel = null;
				int cur = this.cur.getAndIncrement();
				if (this.cur.get() > channels.size()) {
					this.cur.set(this.cur.intValue() - channels.size());
				}
				for (int i = 0; i < channels.size(); i++) {
					int j = cur + i;
					if (j >= channels.size()) {
						j %= channels.size();
					}
					clientChannel = channels.get(j);
					if (clientChannel.isActive()) {
						break;
					}
				}
				return clientChannel;
			} finally {
				readLock.unlock();
			}
		}
	}

	class ReconnectListener implements ChannelFutureListener {

		private OutboundChannel outboundChannel;
		private ChannelFutureListener reconnectOnClose;
		private Runnable reconnectDelay;

		public ReconnectListener(OutboundChannel clientChannel) {
			this.outboundChannel = clientChannel;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (future.isSuccess()) {
				outboundChannel.resetDelay();
				if (reconnectOnClose == null) {
					reconnectOnClose = new ChannelFutureListener() {
						@Override
						public void operationComplete(ChannelFuture future) throws Exception {
							connect(outboundChannel);
						}
					};
				}
				future.channel().closeFuture().addListener(reconnectOnClose);
			} else {
				if (reconnectDelay == null) {
					reconnectDelay = new Runnable() {
						@Override
						public void run() {
							connect(outboundChannel);
						}
					};
				}
				future.channel().eventLoop().schedule(reconnectDelay, outboundChannel.getNextDelay(), TimeUnit.SECONDS);
			}
		}
	}

	static class MultipleConnectListener implements ChannelFutureListener {

		private int state = 0;
		private List<ChannelFuture> futures = Lists.newLinkedList();

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (state == 0) {
				synchronized (MultipleConnectListener.this) {
					if (future.isSuccess()) {
						state = 1;
						MultipleConnectListener.this.notifyAll();
					} else {
						futures.remove(future);
						if (futures.size() == 0) {
							state = -1;
							MultipleConnectListener.this.notifyAll();
						}
					}
				}
			}
		}

		public synchronized ChannelFuture getActiveCf() {
			for (ChannelFuture cf : futures) {
				if (cf.isSuccess()) {
					return cf;
				}
			}
			return null;
		}

		public int getState() {
			return state;
		}
	}

	static class HandshakeListener implements ChannelFutureListener {

		private OutboundChannel oc;
		
		public HandshakeListener(OutboundChannel outboundChannel) {
			this.oc = outboundChannel;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			ChannelPromise handshakePromise = oc.getHandshakePromise();
			if (future.isSuccess()) {
				ClientHandshake clientHandshake = oc.getClientHandshake();
				Handshake handshake = new Handshake();
				handshake.setSuccess(true);
				handshake.setApp(oc.getApp());
				handshake.setMyUuid("this-is-client-uuid");
				handshake.setProperties(clientHandshake.prepare());
				future.channel().writeAndFlush(handshake);
				return;
			}
			if (future.isCancelled()) {
				handshakePromise.cancel(true);
			}
			if (!future.isSuccess()) {
				handshakePromise.setFailure(future.cause());
			}
		}

	}
	
}
