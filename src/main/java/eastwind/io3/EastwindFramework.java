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
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eastwind.io3.obj.Shake;
import eastwind.io3.support.MillisX10Sequence;
import eastwind.io3.support.NamedThreadFactory;
import eastwind.io3.transport.ClientRepository;
import eastwind.io3.transport.ServerRepository;
import eastwind.io3.transport.TransportFactory;

public class EastwindFramework extends App implements Registrable {

	private static Logger logger = LoggerFactory.getLogger(EastwindFramework.class);

	private Bootstrap bootstrap = new Bootstrap();
	private ServerBootstrap serverBootstrap;
	
	private Sequence sequence = new MillisX10Sequence();
	private TransportFactory transportFactory;
	private TransmitSustainer transmitSustainer = new TransmitSustainer();
	private NetServerConfigurer netServerConfigurer = new NetServerConfigurer();
	private ServerRepository serverRepository;
	private ClientRepository clientRepository = new ClientRepository();
	
	private FrameworkObjectHandler frameworkObjectHandler;
	private ObjectHandlerRegistry objectHandlerRegistry = new ObjectHandlerRegistry();
	private int port = 12468;

	private int threads = 256;
	private ThreadPoolExecutor executor;
	private AtomicBoolean started = new AtomicBoolean(false);
	
	public EastwindFramework(String group, boolean server) {
		super(group);
		super.uuid = UUID.randomUUID().toString();
		Shake shake = new Shake(group, super.uuid);
		transportFactory = new TransportFactory(bootstrap, shake, sequence, transmitSustainer);
		if (server) {
			serverBootstrap = new ServerBootstrap();
			serverRepository = new ServerRepository(transportFactory);
		}
		frameworkObjectHandler = new FrameworkObjectHandler(shake, transportFactory, clientRepository, serverRepository);
	}

	public void start() {
		executor = new ThreadPoolExecutor(4, threads, 6, TimeUnit.MINUTES, new SynchronousQueue<Runnable>(),
				new NamedThreadFactory("message-executor"));

		bootstrap.group(new NioEventLoopGroup(2)).channel(NioSocketChannel.class);
		final HeadedObjectCodec headedObjectCodec = new HeadedObjectCodec();
		final DispatcherHandler cDispatcherHandler = new DispatcherHandler(false, frameworkObjectHandler);
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel sc) throws Exception {
				sc.pipeline().addLast("codec", new ObjectCodec());
				sc.pipeline().addLast("headedObjectCodec", headedObjectCodec);
				sc.pipeline().addLast("dispatcherHandler", cDispatcherHandler);
			}
		});

		if (serverBootstrap != null) {
			final DispatcherHandler sDispatcherHandler = new DispatcherHandler(true, frameworkObjectHandler);
			serverBootstrap.group(new NioEventLoopGroup(2), new NioEventLoopGroup());
			serverBootstrap.channel(NioServerSocketChannel.class);
			serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel sc) throws Exception {
					sc.pipeline().addLast("codec", new ObjectCodec());
					sc.pipeline().addLast("headedObjectCodec", headedObjectCodec);
					sc.pipeline().addLast("dispatcherHandler", sDispatcherHandler);
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

	public void setPort(int port) {
		this.port = port;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	public <T> T createInvoker(String group, Class<T> interf) {
		checkStart();
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

	private void checkStart() {
		if (!started.get() && started.compareAndSet(false, true)) {
			start();
		}
	}

	public TransportFactory getTransportFactory() {
		return transportFactory;
	}
	
}
