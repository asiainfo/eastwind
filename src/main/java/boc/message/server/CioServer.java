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

	private ServerContext serverContext = new ServerContext();

	private int channelTimeout = 15;

	private int parentThreads = 0;
	private int childThreads = 0;

	private boolean checkPing = false;
	
	public CioServer(String app) {
		this.app = app;
		registerProvider(new HelloProviderImpl());
	}

	public void start() {
		serverBootstrap = new ServerBootstrap();
		serverBootstrap.group(new NioEventLoopGroup(parentThreads), new NioEventLoopGroup(childThreads));
		serverBootstrap.channel(NioServerSocketChannel.class);
		serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel sc) throws Exception {
				if (checkPing) {
					sc.pipeline().addLast("readTimeoutHandler", new ReadTimeoutHandler(channelTimeout));
				}
				sc.pipeline().addLast("lengthFieldPrepender", new LengthFieldPrepender(2, true));
				sc.pipeline().addLast("lengthDecoder", new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));
				sc.pipeline().addLast("codec", new CioCodec(app, KryoFactory.getKryo()));
				sc.pipeline().addLast("inboundHandler",
						new ServerInboundHandler(filters, exceptionResolver, serverContext));
			}
		});
		serverBootstrap.option(ChannelOption.SO_REUSEADDR, true);
		serverBootstrap.bind("127.0.0.1", port).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				System.out.println(app + ":" + port + " start ok");
			}
		});

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				CioServer.shutdown = true;
				for (int i = 0; i < 30; i++) {
					if (serverContext.getRequestPool().count() == 0) {
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
		this.channelTimeout = channelTimeout;
	}

	public void setCheckPing(boolean checkPing) {
		this.checkPing = checkPing;
	}

	public void setExceptionResolver(ExceptionResolver exceptionResolver) {
		this.exceptionResolver = exceptionResolver;
	}

	public ServerContext getServerContext() {
		return serverContext;
	}

	public void registerProvider(Object handlerObj) {
		serverContext.getProviderManager().register(handlerObj);
	}
}
