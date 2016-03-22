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

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eastwind.io.common.Host;
import eastwind.io2.ObjectCodec;

public class EastwindFramework extends GenericApplication implements Registrable {

	private static Logger logger = LoggerFactory.getLogger(EastwindFramework.class);

	private Bootstrap bootstrap = new Bootstrap();
	private ServerBootstrap serverBootstrap;
	private ObjectHandlerRegistry objectHandlerRegistry = new ObjectHandlerRegistry();
	private int port = 12468;

	private ApplicationManager applicationManager = new ApplicationManager(this, bootstrap);

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
		bootstrap.group(new NioEventLoopGroup(2)).channel(NioSocketChannel.class);
		final FrameworkInboundHandler frameworkHandler = new FrameworkInboundHandler(false, this, applicationManager);
		final ObjectInboundHandler objectHandler = new ObjectInboundHandler(objectHandlerRegistry, applicationManager);
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
			final FrameworkInboundHandler serverFrameworkHandler = new FrameworkInboundHandler(true, this,
					applicationManager);
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

	public TransportableApplicationGroup setApplicationConfig(String group, List<Host> hosts) {
		return applicationManager.setApplicationConfig(group, hosts);
	}

	@Override
	public <T> void registerMessageListener(MessageListener<T> messageListener) {
		objectHandlerRegistry.registerMessageListener(messageListener);
	}

	@Override
	public void registerRpcHandler(Object instance) {
		objectHandlerRegistry.registerRpcHandler(instance);
	}

}
