package boc.message.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.util.List;
import java.util.concurrent.TimeUnit;

import boc.message.CioCodec;
import boc.message.common.KryoFactory;

import com.google.common.collect.Lists;

public class CioServer {

	public static volatile boolean shutdown = false;

	private String app;

	private ServerBootstrap serverBootstrap;
	private int port;

	private List<Filter> filters = Lists.newArrayList();
	private ExceptionResolver exceptionResolver;

	private CioServerContext cioServerContext = new CioServerContext();

	private int ChannelTimeout = 15;

	private int parentThreads = 0;
	private int childThreads = 0;

	private boolean checkPing = true;
	
	public CioServer(String app) {
		this.app = app;
		registerProvider(new CioProviderImpl());
	}

	public void start() {
		serverBootstrap = new ServerBootstrap();
		serverBootstrap.group(new NioEventLoopGroup(parentThreads), new NioEventLoopGroup(childThreads));
		serverBootstrap.channel(NioServerSocketChannel.class);
		serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel sc) throws Exception {
				if (checkPing) {
					sc.pipeline().addLast("readTimeoutHandler", new ReadTimeoutHandler(ChannelTimeout));
				}
				sc.pipeline().addLast("lengthFieldPrepender", new LengthFieldPrepender(2, true));
				sc.pipeline().addLast("lengthDecoder", new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));
				sc.pipeline().addLast("codec", new CioCodec(app, KryoFactory.getKryo()));
				sc.pipeline().addLast("inboundHandler",
						new ServerInboundHandler(filters, exceptionResolver, cioServerContext));
			}
		});
		serverBootstrap.option(ChannelOption.SO_REUSEADDR, true);
		serverBootstrap.bind("127.0.0.1", port).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				System.out.println("start ok");
			}
		});

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				CioServer.shutdown = true;
				for (int i = 0; i < 30; i++) {
					if (cioServerContext.getRequestPool().count() == 0) {
						break;
					}

					try {
						TimeUnit.SECONDS.sleep(1);
					} catch (InterruptedException e) {
						// break?
						break;
					}
				}
			}
		});
	}

	public void setParentThreads(int parentThreads) {
		this.parentThreads = parentThreads;
	}

	public void setChildThreads(int childThreads) {
		this.childThreads = childThreads;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void addFilter(Filter filter) {
		this.filters.add(filter);
	}

	public void setChannelTimeout(int channelTimeout) {
		ChannelTimeout = channelTimeout;
	}

	public void setCheckPing(boolean checkPing) {
		this.checkPing = checkPing;
	}

	public void setExceptionResolver(ExceptionResolver exceptionResolver) {
		this.exceptionResolver = exceptionResolver;
	}

	public CioServerContext getCioServerContext() {
		return cioServerContext;
	}

	public void registerProvider(Object handlerObj) {
		cioServerContext.getProviderManager().register(handlerObj);
	}
}
