package eastwind.io.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;

import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.Charset;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Sharable
public class HttpConsoleHandler extends ChannelInboundHandlerAdapter {

	public static final String NAME = "httpConsoleHandler";

	private TemplateEngine templateEngine;

	public HttpConsoleHandler() {
		templateEngine = new TemplateEngine();
		ClassLoaderTemplateResolver tr = new ClassLoaderTemplateResolver();
		tr.setCacheable(false);
		tr.setPrefix("/view/");
		tr.setSuffix(".html");
		templateEngine.setTemplateResolver(tr);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		FullHttpRequest fhrq = (FullHttpRequest) msg;
		URI uri = new URI(fhrq.uri());
		IContext context = new Context();
		ByteBuf buf = ctx.alloc().directBuffer();
		if (uri.getPath().equals("/")) {
			templateEngine.process("index", context,
					new OutputStreamWriter(new ByteBufOutputStream(buf), Charset.forName("utf-8")));
		}

		FullHttpResponse fhrp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
		HttpUtil.setContentLength(fhrp, buf.readableBytes());
		fhrp.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
		InnerHttpUtil.respond(ctx, fhrq, fhrp);
	}

}
