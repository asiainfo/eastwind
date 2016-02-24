package eastwind.io2.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;
import io.netty.util.AttributeKey;

import java.util.Map;

import com.google.common.collect.Maps;

import eastwind.io.common.Host;
import eastwind.io2.client.OutboundChannelManager.ReconnectListener;

public class OutboundChannel {

	public static final AttributeKey<OutboundChannel> OUTBOUND_CHANNEL = AttributeKey.valueOf("OUTBOUND_CHANNEL");

	public static void set(Channel channel, OutboundChannel clientChannel) {
		channel.attr(OUTBOUND_CHANNEL).set(clientChannel);
	}

	public static OutboundChannel get(Channel channel) {
		return channel.attr(OUTBOUND_CHANNEL).get();
	}

	private String app;
	private Host host;

	private int connectedTimes;
	private volatile long lastConnectedTime;
	private volatile long lastCloseTime;

	private int delay;
	private Map<Long, Rpc<?>> rpcPool = Maps.newConcurrentMap();
	private ClientHandshake clientHandshake;
	private ChannelPromise handshakePromise;
	private ReconnectListener reconnectListener;

	private Map<Long, RpcMediacy> rpcMediacys = Maps.newConcurrentMap();
	
	public OutboundChannel(Host host) {
		this.host = host;
	}

	public void addRpcMediacy(RpcMediacy rpcMediacy) {
		rpcMediacys.put(rpcMediacy.getRpc().getId(), rpcMediacy);
	}
	
	public RpcMediacy removeRpcMediacy(Long id) {
		return rpcMediacys.remove(id);
	}
	
	public Map<Long, RpcMediacy> getRpcMediacys() {
		return rpcMediacys;
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

	public void addRpc(Rpc<?> rpc) {
		this.rpcPool.put(rpc.getId(), rpc);
	}

	public Rpc<?> remove(Long id) {
		return rpcPool.remove(id);
	}

	public Map<Long, Rpc<?>> getRpcPool() {
		return rpcPool;
	}

	public ClientHandshake getClientHandshake() {
		return clientHandshake == null ? DefaultClientHandshake.INSTANCE : clientHandshake;
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
		return handshakePromise.isSuccess() && handshakePromise.channel().isActive();
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
