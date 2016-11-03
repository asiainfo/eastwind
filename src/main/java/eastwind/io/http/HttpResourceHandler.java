package eastwind.io.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;

import com.google.common.io.Files;

@Sharable
public class HttpResourceHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		FullHttpRequest fhrq = (FullHttpRequest) msg;
		URI uri = new URI(fhrq.uri());
		String extension = Files.getFileExtension(uri.getPath());
		if (StringUtils.isEmpty(extension) || extension.equals("json") || fhrq.method() != HttpMethod.GET) {
			ctx.fireChannelRead(fhrq);
			return;
		}

		Path path = Paths.get("", uri.getPath());
		ClassPathResource r = new ClassPathResource(path.toString());
		if (r.exists()) {
			String etag = fhrq.headers().get(HttpHeaderNames.IF_NONE_MATCH);
			if (etag != null && etag.equals(wetag(r.length(), r.lastModified()))) {
				DefaultFullHttpResponse fhre = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
						HttpResponseStatus.NOT_MODIFIED);
				fhre.headers().set(HttpHeaderNames.ETAG, etag);
				InnerHttpUtil.respond(ctx, fhrq, fhre);
				return;
			}
			etag = wetag(r.length(), r.lastModified());

			DefaultHttpResponse dhr = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

			dhr.headers().set(HttpHeaderNames.ETAG, etag);
			dhr.headers().set(HttpHeaderNames.CONTENT_TYPE, MimeType.getType(extension));
			HttpUtil.setContentLength(dhr, r.length());
			ByteBuf buf = ctx.alloc().buffer(1024);
			TransferProgress progress = new TransferProgress(ctx, fhrq, buf, r.getInputStream());
			ctx.channel().writeAndFlush(dhr).addListener(progress);
		} else {
			InnerHttpUtil.notFound(ctx, fhrq);
		}
	}

	private static String wetag(long length, long lastModified) {
		return "W/\"" + length + "-" + lastModified + "\"";
	}
	
	private static class TransferProgress implements GenericFutureListener<ChannelFuture> {

		private ChannelHandlerContext ctx;
		private FullHttpRequest fhr;
		private ByteBuf buf;
		private InputStream inputStream;

		public TransferProgress(ChannelHandlerContext ctx, FullHttpRequest fhr, ByteBuf buf, InputStream inputStream) {
			this.ctx = ctx;
			this.fhr = fhr;
			this.buf = buf;
			this.inputStream = inputStream;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (future.isSuccess()) {
				buf.clear();
				int len = buf.writeBytes(inputStream, 1024);
				if (len < 1024) {
					ctx.write(buf);
					InnerHttpUtil.respond(ctx, fhr, LastHttpContent.EMPTY_LAST_CONTENT);
				} else {
					buf.retain();
					ctx.writeAndFlush(buf).addListener(this);
				}
			} else {
				if (buf.refCnt() > 0) {
					ReferenceCountUtil.release(buf.refCnt());
				}
			}
		}
	}

}
