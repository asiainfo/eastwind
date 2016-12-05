package eastwind.io;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import java.util.List;

import com.google.common.collect.Lists;

import eastwind.io.http.HttpDispatcherHandler;
import eastwind.io.support.InnerUtils;

@Sharable
public class ChannelInitializeHandler extends ChannelInboundHandlerAdapter {

	private static List<byte[]> HTTP_BEGINNING = Lists.newArrayList("GET ".getBytes(), "PUT ".getBytes(),
			"HEAD ".getBytes(), "POST ".getBytes(), "TRACE ".getBytes(), "DELETE ".getBytes(), "OPTIONS ".getBytes(),
			"CONNECT ".getBytes());

	private ServerContext serverContext;
	private HttpDispatcherHandler httpDispatcherHandler;

	public ChannelInitializeHandler(ServerContext serverContext) {
		this.serverContext = serverContext;
		this.httpDispatcherHandler = new HttpDispatcherHandler(serverContext);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ChannelPipeline pipeline = ctx.pipeline();
		ByteBuf in = (ByteBuf) msg;
		if (in.readableBytes() > 0) {
			byte magic = in.getByte(0);
			if (magic >= 0 && magic < 32) {
				// rpc
			} else {
				for (byte[] pattern : HTTP_BEGINNING) {
					int i = 0;
					int len = pattern.length;
					for (; i < len && i < in.readableBytes(); i++) {
						if (in.getByte(i) != pattern[i]) {
							break;
						}
					}
					if (i == len) {
						// http
						pipeline.addLast(new HttpServerCodec());
						pipeline.addLast(new HttpObjectAggregator(1024));
						pipeline.addLast(InnerUtils.getName(HttpDispatcherHandler.class), httpDispatcherHandler);
						pipeline.remove(this);
						ctx.fireChannelRead(in);
						break;
					}
					if (i == in.readableBytes()) {
						break;
					} else if (pattern == HTTP_BEGINNING.get(HTTP_BEGINNING.size() - 1)) {
						// unknow
					}
				}
			}
		}
	}

}
