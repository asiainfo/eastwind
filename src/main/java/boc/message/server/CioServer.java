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

	public volatile boolean isShutdown = false;

	private String app;

	private ServerBootstrap serverBootstrap;
	private int port;

	private ProviderManager providerManager = new ProviderManager();
	private ServerHandshaker serverHandshaker;
	private List<Filter> filters = Lists.newArrayList();

	private ServerCount serverCount = new ServerCount();

	private int channelTimeout = 15;

	private int parentThreads = 0;
	private int childThreads = 0;

	private boolean checkPing = false;

	public CioServer(String app) {
		this.app = app;
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
				if (serverHandshaker != null) {
					sc.pipeline().addLast(new ServerHandshakeHandler(serverHandshaker));
				}
				sc.pipeline().addLast(
						new ServerInboundHandler(filters, providerManager, serverCount));
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
				shutdown();
				for (int i = 0; i < 30; i++) {
					if (serverCount.getHandlingCount() == 0) {
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

	public void shutdown() {
		isShutdown = true;
		serverCount.shutdown();
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

	public void setServerHandshaker(ServerHandshaker serverHandshaker) {
		this.serverHandshaker = serverHandshaker;
	}

	public ServerCount getServerCount() {
		return serverCount;
	}

	public void registerProvider(Object provider) {
		providerManager.register(provider);
	}
}
