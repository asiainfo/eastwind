package eastwind.io.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;

import java.util.List;

public class InnerHttpUtil {

	public static String get(QueryStringDecoder dec, String name) {
		List<String> l = dec.parameters().get(name);
		return l == null ? null : l.get(0);
	}

	public static ChannelFuture respond(ChannelHandlerContext ctx, HttpRequest hr, Object message) {
		ChannelFuture cf = ctx.writeAndFlush(message);
		if (!HttpUtil.isKeepAlive(hr)) {
			cf.addListener(ChannelFutureListener.CLOSE);
		}
		return cf;
	}

	public static void notFound(ChannelHandlerContext ctx, HttpRequest hr) {
		ByteBuf buf = Unpooled.copiedBuffer("Failure: " + 404 + "\r\n", CharsetUtil.UTF_8);
		FullHttpResponse fhrp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND,
				buf);
		fhrp.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
		HttpUtil.setContentLength(fhrp, buf.readableBytes());
		respond(ctx, hr, fhrp);
	}
}
