package eastwind.io;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;
import io.netty.util.AttributeKey;

public class ChannelAttr {

	public static ThreadLocal<Channel> CHANNEL_TL = new ThreadLocal<Channel>();

	public static final AttributeKey<Integer> ID = AttributeKey.valueOf("ID");
	public static final AttributeKey<ChannelPromise> HANDSHAKE_PROMISE = AttributeKey.valueOf("HANDSHAKE_PROMISE");
	public static final AttributeKey<Session> SESSION = AttributeKey.valueOf("SESSION");
	public static final AttributeKey<String> APP = AttributeKey.valueOf("APP");

	public static void setId(Channel channel, int id) {
		set(channel, ID, id);
	}
	
	public static int getId(Channel channel) {
		return get(channel, ID);
	}

	public static ChannelPromise initHandShakePromise(Channel channel) {
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
