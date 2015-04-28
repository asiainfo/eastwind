package eastwind.io.nioclient;

import io.netty.channel.ChannelFuture;

import java.util.concurrent.atomic.AtomicInteger;

import eastwind.io.common.Host;

public class ConnectStat {

	private static final int INITIAL_DELAY = 100;
	
	private AtomicInteger mod = new AtomicInteger();
	
	private String app;
	private Host host;
	private ClientHandshaker clientHandshaker;

	private ChannelFuture channelFuture;

	private int i = 0;
	private int delay = INITIAL_DELAY;

	private long lastConnectTime;
	private boolean stoped;
	private boolean connecting;
	
	public ConnectStat(String app, Host host, ClientHandshaker clientHandshaker) {
		this.app = app;
		this.host = host;
		this.clientHandshaker = clientHandshaker;
	}

	public int getMode() {
		return mod.intValue();
	}
	
	public void incrementMod() {
		mod.getAndIncrement();
	}
	
	public Host getHost() {
		return host;
	}

	public String getApp() {
		return app;
	}

	public boolean isStoped() {
		return stoped;
	}

	public int getDelay() {
		return delay > 15000 ? delay : (delay += 200 * i++);
	}

	public void resetDelay() {
		this.i = 0;
		this.delay = INITIAL_DELAY;
	}
	
	public long getLastConnectTime() {
		return lastConnectTime;
	}

	public void refreshLastConnectTime() {
		this.lastConnectTime = System.currentTimeMillis();
	}
	
	public void setStoped(boolean stoped) {
		this.stoped = stoped;
	}

	public ClientHandshaker getClientHandshaker() {
		return clientHandshaker;
	}

	public ChannelFuture getChannelFuture() {
		return channelFuture;
	}

	public boolean isConnecting() {
		return connecting;
	}

	public void setConnecting(boolean connecting) {
		this.connecting = connecting;
	}

	public void setChannelFuture(ChannelFuture channelFuture) {
		this.channelFuture = channelFuture;
	}
}
