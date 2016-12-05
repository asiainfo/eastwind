package eastwind.io;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import eastwind.io.transport.ClientRepository;

public class EastWindServer extends EastWindClient {

	private int port = 12468;
	private ServerBootstrap serverBootstrap;
	private ClientRepository clientRepository;
	private ServerContext serverContext = new ServerContext(group, uuid, sequence);
	private ServerFrameworkHandler serverFrameworkHandler;
	private ServerBusinessHandler serverBusinessHandler;
	private AtomicBoolean started = new AtomicBoolean(false);

	public EastWindServer(String group) {
		super(group);
		clientRepository = new ClientRepository();
		serverBootstrap = new ServerBootstrap();
//		serverFrameworkHandler = new ServerFrameworkHandler(shake, transmitSustainer, serverContainer,
//				transportFactory, clientRepository);
//		serverBusinessHandler = new ServerBusinessHandler(executor, serverContainer);
	}

	public void start() {
		super.start();
		
		if (!started.get() && started.compareAndSet(false, true)) {
			initSerializer();
			serverBootstrap.group(new NioEventLoopGroup(2), new NioEventLoopGroup());
			serverBootstrap.channel(NioServerSocketChannel.class);
			serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel sc) throws Exception {
					ChannelPipeline pipeline = sc.pipeline();
					pipeline.addLast("initializer", new ChannelInitializeHandler(serverContext));
//					pipeline.addLast("objectCodec", new ObjectCodec(serializerFactoryHolder, handlerRegistry,
//							transmitSustainer));
//					pipeline.addLast("headedObjectCodec", new HeadedObjectCodec());
//					pipeline.addLast("serverFrameworkHandler", serverFrameworkHandler);
//					pipeline.addLast("serverBusinessHandler", serverBusinessHandler);
				}
			});

			serverBootstrap.option(ChannelOption.SO_REUSEADDR, true);
			serverBootstrap.bind(port).addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					SocketAddress socketAddress = future.channel().localAddress();
					serverContext.setSocketAddress(socketAddress);
					logger.info("{} started:{}", group, socketAddress);
				}
			});
		}
	}

	public void registerHandler(Object instance) {
		serverContext.getProviderRegistry().registerHandler(instance);
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
