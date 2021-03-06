package eastwind.io;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import eastwind.io.transport.ClientRepository;

public class EastWindServer extends EastWindClient {

	private int port = 12468;
	private ServerBootstrap serverBootstrap;
	private ClientRepository clientRepository;
	private ServerContext serverContext = new ServerContext(group, uuid, sequence);
	private AtomicBoolean started = new AtomicBoolean(false);

	public EastWindServer(String group) {
		super(group);
		clientRepository = new ClientRepository();
		serverBootstrap = new ServerBootstrap();
	}

	public void start() {
		super.start();

		if (!started.get() && started.compareAndSet(false, true)) {
			initSerializer();
			serverBootstrap.group(new NioEventLoopGroup(2), new NioEventLoopGroup());
			serverBootstrap.channel(NioServerSocketChannel.class);
			ServerFrameworkHandler serverFrameworkHandler = new ServerFrameworkHandler(shake, transmitSustainer,
					serverContext, transportFactory, clientRepository);
			ServerBusinessHandler serverBusinessHandler = new ServerBusinessHandler(executor, serverContext);
			serverBootstrap.childHandler(new ChannelInitializeHandler(serverContext, serverFrameworkHandler,
					serverBusinessHandler, serializerFactoryHolder, transmitSustainer));

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

	public void registerProvider(Object instance) {
		serverContext.getProviderRegistry().registerProvider(instance);
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
