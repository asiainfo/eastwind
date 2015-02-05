package boc.message;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public class ChannelStat {

	public static final AttributeKey<Integer> gid = AttributeKey.valueOf("gid");

	public static final AttributeKey<Long> lastWriteTime = AttributeKey.valueOf("lastWriteTime");
	
	public static <T> T get(Channel channel, AttributeKey<T> key) {
		return channel.attr(key).get();
	}

	public static <T> void set(Channel channel, AttributeKey<T> key, T value) {
		channel.attr(key).set(value);
	}
}
