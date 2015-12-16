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

public class ClientChannelManager {

	private Bootstrap bootstrap;

	private ApplicationConfigManager applicationConfigManager;

	private ConcurrentMap<String, AppClientChannel> appClientChannels = Maps.newConcurrentMap();

	public ClientChannelManager(Bootstrap bootstrap, ApplicationConfigManager applicationConfigManager) {
		this.bootstrap = bootstrap;
		this.applicationConfigManager = applicationConfigManager;
	}

	public ClientChannel getActiveClientChannel(String app) {
		AppClientChannel appClientChannel = getAppClientChannel(app);
		ClientChannel cc = appClientChannel.getActiveClientChannel();
		if (cc != null) {
			return cc;
		}
		List<ClientChannel> clientChannels = appClientChannel.clientChannels;
		int size = clientChannels.size();
		MultipleConnectListener listener = new MultipleConnectListener();
		for (int i = 0; i < size; i++) {
			cc = clientChannels.get(i);
			connect(cc);
			ChannelPromise cp = cc.getHandshakePromise();
			if (cp != null) {
				cp.addListener(listener);
				listener.futures.add(cp);
			}
		}
		synchronized (listener) {
			if (listener.getState() == 1) {
				ChannelFuture cf = listener.getActiveCf();
				return ClientChannel.get(cf.channel());
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
			return ClientChannel.get(cf.channel());
		}
		// throw ex
		return null;
	}

	public ClientChannel getClientChannel(String app, Host host) {
		AppClientChannel appClientChannel = getAppClientChannel(app);
		ClientChannel cc = appClientChannel.getClientChannel(host);
		if (cc != null) {
			if (cc.getHandshakePromise() == null) {
				connect(cc);
			}
			return cc;
		}
		appClientChannel.writeLock.lock();
		try {
			cc = appClientChannel.getClientChannel(host);
			if (cc == null) {
				cc = new ClientChannel(host);
				appClientChannel.addClientChannel(cc);
			}
		} finally {
			appClientChannel.writeLock.unlock();
		}
		connect(cc);
		return cc;
	}

	public ApplicationConfigManager getApplicationConfigManager() {
		return applicationConfigManager;
	}

	private AppClientChannel getAppClientChannel(String app) {
		AppClientChannel appClientChannel = this.appClientChannels.get(app);
		if (appClientChannel == null) {
			appClientChannel = new AppClientChannel();
			appClientChannel = CommonUtils.putIfAbsent(appClientChannels, app, appClientChannel);
		}
		return appClientChannel;
	}

	private void connect(ClientChannel cc) {
		synchronized (cc) {
			if (cc.getHandshakePromise() != null) {
				return;
			}
			Host host = cc.getHost();
			ChannelFuture cf = bootstrap.connect(host.getIp(), host.getPort());
			ChannelPromise handshakePromise = cf.channel().newPromise();
			cc.setHandshakePromise(handshakePromise);
			ClientChannel.set(cf.channel(), cc);

			ReconnectListener reconnectListener = cc.getReconnectListener();
			if (reconnectListener == null) {
				reconnectListener = new ReconnectListener(cc);
				cc.setReconnectListener(reconnectListener);
			}
			cf.addListener(new HandshakeFailListener(handshakePromise));
			cf.addListener(reconnectListener);
		}
	}

	private static class AppClientChannel {

		AtomicInteger cur = new AtomicInteger(0);
		List<ClientChannel> clientChannels = Lists.newArrayList();
		ReadWriteLock lock = new ReentrantReadWriteLock();
		Lock readLock = lock.readLock();
		Lock writeLock = lock.writeLock();

		void addClientChannel(ClientChannel clientChannel) {
			this.clientChannels.add(clientChannel);
		}

		ClientChannel getClientChannel(Host host) {
			readLock.lock();
			try {
				for (ClientChannel cc : clientChannels) {
					if (cc != null && cc.getHost().equals(host)) {
						return cc;
					}
				}
				return null;
			} finally {
				readLock.unlock();
			}
		}

		ClientChannel getActiveClientChannel() {
			try {
				readLock.lock();
				ClientChannel clientChannel = null;
				int cur = this.cur.getAndIncrement();
				if (this.cur.get() > clientChannels.size()) {
					this.cur.set(this.cur.intValue() - clientChannels.size());
				}
				for (int i = 0; i < clientChannels.size(); i++) {
					int j = cur + i;
					if (j >= clientChannels.size()) {
						j %= clientChannels.size();
					}
					clientChannel = clientChannels.get(j);
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

		private ClientChannel clientChannel;
		private ChannelFutureListener reconnectOnClose;
		private Runnable reconnectDelay;

		public ReconnectListener(ClientChannel clientChannel) {
			this.clientChannel = clientChannel;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (future.isSuccess()) {
				clientChannel.resetDelay();
				if (reconnectOnClose == null) {
					reconnectOnClose = new ChannelFutureListener() {
						@Override
						public void operationComplete(ChannelFuture future) throws Exception {
							connect(clientChannel);
						}
					};
				}
				future.channel().closeFuture().addListener(reconnectOnClose);
			} else {
				if (reconnectDelay == null) {
					reconnectDelay = new Runnable() {
						@Override
						public void run() {
							connect(clientChannel);
						}
					};
				}
				future.channel().eventLoop().schedule(reconnectDelay, clientChannel.getNextDelay(), TimeUnit.SECONDS);
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

	static class HandshakeFailListener implements ChannelFutureListener {

		private ChannelPromise handshakePromise;

		public HandshakeFailListener(ChannelPromise handshakePromise) {
			this.handshakePromise = handshakePromise;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (future.isCancelled()) {
				handshakePromise.cancel(true);
			}
			if (!future.isSuccess()) {
				handshakePromise.setFailure(future.cause());
			}
			ClientChannel.get(future.channel()).setHandshakePromise(null);
		}

	}
}
