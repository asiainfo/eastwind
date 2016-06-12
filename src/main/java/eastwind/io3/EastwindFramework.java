package eastwind.io3;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.UUID;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eastwind.io.common.NamedThreadFactory;
import eastwind.io2.ObjectCodec;

public class EastwindFramework extends Application implements Registrable {

	private static Logger logger = LoggerFactory.getLogger(EastwindFramework.class);

	private Bootstrap bootstrap = new Bootstrap();
	private ServerBootstrap serverBootstrap;
	private TransportSustainer transportSustainer = new TransportSustainer(new MillisX10Sequence());
	private ObjectHandlerRegistry objectHandlerRegistry = new ObjectHandlerRegistry();
	private int port = 12468;

	private int threads = 256;
	private ThreadPoolExecutor executor;

	private ApplicationManager applicationManager = new ApplicationManager(transportSustainer, bootstrap);

	public EastwindFramework(String group) {
		this(group, true);
	}

	public EastwindFramework(String group, boolean server) {
		super(group);
		super.uuid = UUID.randomUUID().toString();
		if (server) {
			serverBootstrap = new ServerBootstrap();
		}
	}

	public void start() {
		executor = new ThreadPoolExecutor(4, threads, 6, TimeUnit.MINUTES, new SynchronousQueue<Runnable>(),
				new NamedThreadFactory("message-executor"));

		bootstrap.group(new NioEventLoopGroup(2)).channel(NioSocketChannel.class);
		final FrameworkInboundHandler frameworkHandler = new FrameworkInboundHandler(false, transportSustainer,
				applicationManager, objectHandlerRegistry);
		final ObjectInboundHandler objectHandler = new ObjectInboundHandler(applicationManager, objectHandlerRegistry,
				transportSustainer, executor);
		final HeadedObjectCodec headedObjectCodec = new HeadedObjectCodec();
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel sc) throws Exception {
				sc.pipeline().addLast("codec", new ObjectCodec());
				sc.pipeline().addLast("headedObjectCodec", headedObjectCodec);
				sc.pipeline().addLast("frameworkHandler", frameworkHandler);
				sc.pipeline().addLast("objectHandler", objectHandler);
			}
		});

		if (serverBootstrap != null) {
			serverBootstrap.group(new NioEventLoopGroup(2), new NioEventLoopGroup());
			serverBootstrap.channel(NioServerSocketChannel.class);
			final FrameworkInboundHandler serverFrameworkHandler = new FrameworkInboundHandler(true,
					transportSustainer, applicationManager, objectHandlerRegistry);
			serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel sc) throws Exception {
					sc.pipeline().addLast("codec", new ObjectCodec());
					sc.pipeline().addLast("headedObjectCodec", headedObjectCodec);
					sc.pipeline().addLast("frameworkHandler", serverFrameworkHandler);
					sc.pipeline().addLast("objectHandler", objectHandler);
				}
			});

			serverBootstrap.option(ChannelOption.SO_REUSEADDR, true);
			serverBootstrap.bind(port).addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					logger.info("start at port:{}", port);
				}
			});
		}
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	public ApplicationManager getApplicationManager() {
		return applicationManager;
	}

	public <T> T createInvoker(String group, Class<T> interf) {
		return null;
	}

	@Override
	public <T> void registerListener(MessageListener<T> messageListener) {
		objectHandlerRegistry.registerListener(messageListener);
	}

	@Override
	public void registerHandler(Object instance) {
		objectHandlerRegistry.registerHandler(instance);
	}

}
