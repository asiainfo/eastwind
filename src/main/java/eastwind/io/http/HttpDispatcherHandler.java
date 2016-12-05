package eastwind.io.http;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.net.URI;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

import eastwind.io.ServerContext;
import eastwind.io.support.InnerUtils;

@Sharable
public class HttpDispatcherHandler extends ChannelInboundHandlerAdapter {

	private static List<String> ORDERS = Lists.newArrayList(InnerUtils.getName(HttpDispatcherHandler.class),
			InnerUtils.getName(HttpResourceHandler.class), InnerUtils.getName(HttpConsoleHandler.class),
			InnerUtils.getName(HttpProviderHandler.class));

	private HttpResourceHandler httpResourceHandler = new HttpResourceHandler();
	private HttpConsoleHandler httpConsoleHandler;
	private HttpProviderHandler httpProviderHandler;

	public HttpDispatcherHandler(ServerContext serverContext) {
		httpConsoleHandler = new HttpConsoleHandler(serverContext);
		httpProviderHandler = new HttpProviderHandler(serverContext.getProviderRegistry());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		FullHttpRequest fhrq = (FullHttpRequest) msg;
		URI uri = new URI(fhrq.uri());
		String extension = Files.getFileExtension(uri.getPath());
		ChannelPipeline pipeline = ctx.pipeline();

		ChannelHandler handler = null;

		if (!StringUtils.isBlank(extension) && !extension.equals("json") && fhrq.method() == HttpMethod.GET) {
			handler = httpResourceHandler;
		}

		if (handler == null) {
			if (uri.getPath().equals("/") || new QueryStringDecoder(fhrq.uri()).parameters().get("console") != null) {
				handler = httpConsoleHandler;
			}
		}

		if (handler == null) {
			handler = httpProviderHandler;
		}

		String name = InnerUtils.getName(handler.getClass());
		String baseName = findHandlerBefore(pipeline, name);
		if (pipeline.get(name) == null) {
			pipeline.addAfter(baseName, name, handler);
		}
		pipeline.context(baseName).fireChannelRead(fhrq);
	}

	private String findHandlerBefore(ChannelPipeline pipeline, String name) {
		int i = ORDERS.indexOf(name);
		while (pipeline.get(ORDERS.get(--i)) == null)
			;
		return ORDERS.get(i);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
	}
	
}
