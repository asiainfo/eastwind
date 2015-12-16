package eastwind.io2.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;
import io.netty.util.AttributeKey;
import eastwind.io.common.Host;
import eastwind.io2.client.ClientChannelManager.ReconnectListener;

public class ClientChannel {

	public static final AttributeKey<ClientChannel> CLIENT_CHANNEL = AttributeKey.valueOf("CLIENT_CHANNEL");

	public static void set(Channel channel, ClientChannel clientChannel) {
		channel.attr(CLIENT_CHANNEL).set(clientChannel);
	}

	public static ClientChannel get(Channel channel) {
		return channel.attr(CLIENT_CHANNEL).get();
	}

	private String app;
	private Host host;

	private int connectedTimes;
	private long lastConnectedTime;
	private long lastCloseTime;

	private int delay;
	private ClientHandshake clientHandshake;
	private ChannelPromise handshakePromise;
	private ReconnectListener reconnectListener;

	public ClientChannel(Host host) {
		this.host = host;
	}

	public int getNextDelay() {
		return this.delay >= 15 ? 15 : delay++;
	}

	public void resetDelay() {
		this.delay = 0;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public Host getHost() {
		return host;
	}

	public void setHost(Host host) {
		this.host = host;
	}

	public ClientHandshake getClientHandshake() {
		return clientHandshake;
	}

	public void setClientHandshake(ClientHandshake clientHandshake) {
		this.clientHandshake = clientHandshake;
	}

	public ChannelPromise getHandshakePromise() {
		return handshakePromise;
	}

	public boolean isActive() {
		if (handshakePromise == null) {
			return false;
		}
		return handshakePromise.isSuccess();
	}

	public void setHandshakePromise(ChannelPromise handshakePromise) {
		this.handshakePromise = handshakePromise;
	}

	public ReconnectListener getReconnectListener() {
		return reconnectListener;
	}

	public void setReconnectListener(ReconnectListener reconnectListener) {
		this.reconnectListener = reconnectListener;
	}

	public long getLastConnectedTime() {
		return lastConnectedTime;
	}

	public void setLastConnectedTime(long lastConnectedTime) {
		this.lastConnectedTime = lastConnectedTime;
	}

	public long getLastCloseTime() {
		return lastCloseTime;
	}

	public void setLastCloseTime(long lastCloseTime) {
		this.lastCloseTime = lastCloseTime;
	}

	public void addConnectedTimes() {
		this.connectedTimes++;
	}

	public int getConnectedTimes() {
		return connectedTimes;
	}

}
