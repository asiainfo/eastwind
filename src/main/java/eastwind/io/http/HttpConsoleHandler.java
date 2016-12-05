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
import io.netty.handler.codec.http.QueryStringDecoder;

import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.google.common.base.Splitter;

import eastwind.io.ProviderInstance;
import eastwind.io.ProviderRegistry;
import eastwind.io.ServerContext;
import eastwind.io.invocation.Route;
import eastwind.io.model.Host;
import eastwind.io.support.InnerUtils;

@Sharable
public class HttpConsoleHandler extends ChannelInboundHandlerAdapter {

	public static final String NAME = "httpConsoleHandler";

	private ServerContext serverContext;
	private TemplateEngine templateEngine;

	public HttpConsoleHandler(ServerContext serverContext) {
		templateEngine = new TemplateEngine();
		ClassLoaderTemplateResolver tr = new ClassLoaderTemplateResolver();
		tr.setCacheable(false);
		tr.setPrefix("/view/");
		tr.setSuffix(".html");
		templateEngine.setTemplateResolver(tr);

		this.serverContext = serverContext;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		FullHttpRequest fhrq = (FullHttpRequest) msg;
		URI uri = new URI(fhrq.uri());
		Context context = new Context();
		context.setVariable("eastwind", serverContext);
		ByteBuf buf = ctx.alloc().directBuffer();
		if (uri.getPath().equals("/")) {
			processView("index", context, buf);
		} else {
			QueryStringDecoder query = new QueryStringDecoder(fhrq.uri() + "&"
					+ fhrq.content().toString(Charset.forName("utf-8")));
			PathDecoder decoder = new PathDecoder(uri.getPath());
			if (decoder.getFirst().equals("provider")) {

				ProviderRegistry providerRegistry = serverContext.getProviderRegistry();
				List<ProviderInstance> instances = providerRegistry.getProviderInstances();
				context.setVariable("providers", instances);
				processView("provider", context, buf);
				
			} else if (decoder.getFirst().equals("route")) {

				if (decoder.getSecond() == null) {
					context.setVariable("routes", serverContext.getRouteGroup().getRoutes());
					processView("route", context, buf);
				} else if (decoder.getSecond().equals("add")) {
					String name = getFirstParam(query, "name");
					String precedence = getFirstParam(query, "precedence");

					String consumer = getFirstParam(query, "consumer");
					String fromVersion = getFirstParam(query, "fromVersion");
					String expression = getFirstParam(query, "expression");
					String fromIps = getFirstParam(query, "fromIps");

					String provider = getFirstParam(query, "provider");
					String toVersion = getFirstParam(query, "toVersion");
					String rate = getFirstParam(query, "rate");
					String toHosts = getFirstParam(query, "toHosts");

					Route route = new Route();
					route.setId(serverContext.getSequence().get());
					route.setName(name);
					route.setPrecedence(Integer.parseInt(precedence));

					route.setConsumer(consumer);
					route.setFromVersion(fromVersion);
					route.setExpression(expression);

					if (!StringUtils.isBlank(fromIps)) {
						route.setFromIps(Splitter.on("[,;\r ]").trimResults().omitEmptyStrings().splitToList(fromIps));
					}

					route.setProvider(provider);
					route.setToVersion(toVersion);
					if (!StringUtils.isBlank(rate)) {
						route.setRate(Integer.parseInt(rate));
					}
					if (!StringUtils.isBlank(toHosts)) {
						List<String> l = Splitter.on("[,;\r ]").trimResults().omitEmptyStrings().splitToList(toHosts);
						Set<Host> h = new HashSet<Host>(l.size());
						for (String t : l) {
							h.add(InnerUtils.toHost(t));
						}
						route.setToHosts(h);
					}
					serverContext.getRouteGroup().add(route);
					FullHttpResponse fhrp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
							HttpResponseStatus.MOVED_PERMANENTLY);
					fhrp.headers().set(HttpHeaderNames.LOCATION, "/route?console");
					InnerHttpUtil.respond(ctx, fhrq, fhrp);
					return;
				} else if (decoder.getSecond().equals("remove")) {
					long id = Long.parseLong(getFirstParam(query, "routeid"));
					serverContext.getRouteGroup().remove(id);
					FullHttpResponse fhrp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
							HttpResponseStatus.MOVED_PERMANENTLY);
					fhrp.headers().set(HttpHeaderNames.LOCATION, "/route?console");
					InnerHttpUtil.respond(ctx, fhrq, fhrp);
					return;
				}
			}
		}

		FullHttpResponse fhrp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
		HttpUtil.setContentLength(fhrp, buf.readableBytes());
		fhrp.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
		InnerHttpUtil.respond(ctx, fhrq, fhrp);
	}

	private String getFirstParam(QueryStringDecoder query, String name) {
		List<String> params = query.parameters().get(name);
		return params == null ? null : params.get(0).trim();
	}

	private void processView(String view, Context context, ByteBuf buf) {
		templateEngine.process(view, context,
				new OutputStreamWriter(new ByteBufOutputStream(buf), Charset.forName("utf-8")));
	}

}
