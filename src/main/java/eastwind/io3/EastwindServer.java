package eastwind.io3;

import java.util.concurrent.atomic.AtomicBoolean;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import eastwind.io3.transport.ClientRepository;

public class EastwindServer extends EastwindClient {

	private int port = 12468;
	private ServerBootstrap serverBootstrap;
	private ClientRepository clientRepository;
	private HandlerRegistry handlerRegistry = new HandlerRegistry();
	private ServerFrameworkHandler serverFrameworkHandler;
	private ServerBusinessHandler serverBusinessHandler;
	private AtomicBoolean started = new AtomicBoolean(false);

	public EastwindServer(String group) {
		super(group);
		clientRepository = new ClientRepository();
		serverBootstrap = new ServerBootstrap();
		serverFrameworkHandler = new ServerFrameworkHandler(shake, transmitSustainer, handlerRegistry,
				transportFactory, clientRepository);
		serverBusinessHandler = new ServerBusinessHandler(executor, handlerRegistry);
	}

	public void start() {
		super.start();
		if (!started.get() && started.compareAndSet(false, true)) {
			serverBootstrap.group(new NioEventLoopGroup(2), new NioEventLoopGroup());
			serverBootstrap.channel(NioServerSocketChannel.class);
			serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel sc) throws Exception {
					sc.pipeline().addLast("codec", new ObjectCodec());
					sc.pipeline().addLast("headedObjectCodec", new HeadedObjectCodec());
					sc.pipeline().addLast("serverObjectHandler", serverFrameworkHandler);
					sc.pipeline().addLast("serverBusinessHandler", serverBusinessHandler);
				}
			});

			serverBootstrap.option(ChannelOption.SO_REUSEADDR, true);
			serverBootstrap.bind(port).addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					logger.info("{} started, port:{}", group, port);
				}
			});
		}
	}

	public void registerHandler(Object instance) {
		handlerRegistry.registerHandler(instance);
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
