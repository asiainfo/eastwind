package eastwind.io;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;
import io.netty.util.AttributeKey;
import eastwind.io.common.Host;
import eastwind.io.nioclient.ClientHandshaker;
import eastwind.io.nioclient.InterfAb;

public class ChannelAttr {

	public static ThreadLocal<Channel> CHANNEL_TL = new ThreadLocal<Channel>();

	public static final AttributeKey<Integer> ID = AttributeKey.valueOf("ID");
	public static final AttributeKey<String> REMOTE_APP = AttributeKey.valueOf("REMOTE_APP");
	public static final AttributeKey<InterfAb> INTERF_AB = AttributeKey.valueOf("INTERF_AB");
	public static final AttributeKey<Host> REMOTE_HOST = AttributeKey.valueOf("REMOTE_HOST");
	public static final AttributeKey<ClientHandshaker> CLIENT_HANDSHAKE = AttributeKey.valueOf("CLIENT_HANDSHAKER");
	public static final AttributeKey<ChannelPromise> HANDSHAKE_PROMISE = AttributeKey.valueOf("HANDSHAKE_PROMISE");
	public static final AttributeKey<Session> SESSION = AttributeKey.valueOf("SESSION");

	public static void setId(Channel channel, int id) {
		set(channel, ID, id);
	}

	public static int getId(Channel channel) {
		return get(channel, ID);
	}

	public static ChannelPromise initHandshakePromise(Channel channel) {
		synchronized (channel) {
			ChannelPromise channelPromise = get(channel, HANDSHAKE_PROMISE);
			if (channelPromise == null) {
				channelPromise = channel.newPromise();
				set(channel, HANDSHAKE_PROMISE, channelPromise);
			}
			return channelPromise;
		}
	}

	public static <T> T get(Channel channel, AttributeKey<T> key) {
		return channel.attr(key).get();
	}

	public static <T> void set(Channel channel, AttributeKey<T> key, T value) {
		channel.attr(key).set(value);
	}
}
