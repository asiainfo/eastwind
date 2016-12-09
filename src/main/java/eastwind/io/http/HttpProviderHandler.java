package eastwind.io.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;

import java.net.URI;
import java.nio.charset.Charset;

import org.apache.commons.lang3.math.NumberUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eastwind.io.MethodHandler;
import eastwind.io.ProviderRegistry;
import eastwind.io.serializer.JsonSerializer;
import eastwind.io.support.InnerUtils;

public class HttpProviderHandler extends ChannelInboundHandlerAdapter {

	private ProviderRegistry providerRegistry;
	private ObjectMapper objectMapper = new JsonSerializer().getObjectMapper();

	public HttpProviderHandler(ProviderRegistry providerRegistry) {
		this.providerRegistry = providerRegistry;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		FullHttpRequest fhrq = (FullHttpRequest) msg;
		StringBuilder sb = new StringBuilder();
		URI uri = new URI(fhrq.uri());
		sb.append("[");
		sb.append(uri.getQuery());
		sb.append(fhrq.content().toString(Charset.forName("utf-8")));
		sb.append("]");
		PathDecoder paths = new PathDecoder(uri.getPath());
		String name = InnerUtils.getFullProviderName(paths.getFirst(), paths.getSecond());
		MethodHandler methodHandler = providerRegistry.findHandler(name);
		Class<?>[] pts = methodHandler.getParameterTypes();
		Object[] params = new Object[pts.length];
		if (pts.length == 1) {
			String tmp = sb.substring(1, sb.length() - 1);
			if (!NumberUtils.isNumber(tmp)) {
				if ((tmp.startsWith("\"") && tmp.endsWith("\"")) || (tmp.startsWith("[") && tmp.endsWith("]"))
						|| (tmp.startsWith("{") && tmp.endsWith("}"))) {
				} else {
					tmp = "\"" + tmp + "\"";
				}
			}
			params[0] = objectMapper.readValue(tmp, pts[0]);
		} else if (pts.length > 1) {
			JsonNode root = objectMapper.readTree(sb.toString());
			for (int i = 0; i < pts.length; i++) {
				JsonNode node = root.get(i);
				JsonParser jp = node.traverse();
				params[i] = objectMapper.readValue(jp, pts[i]);
			}
		}
		Object result = methodHandler.invoke(params);
		ByteBuf buf = ctx.alloc().buffer();
		objectMapper.writeValue(new ByteBufOutputStream(buf), result);

		FullHttpResponse fhrp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
		HttpUtil.setContentLength(fhrp, buf.readableBytes());
		fhrp.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
		InnerHttpUtil.respond(ctx, fhrq, fhrp);
	}
}
