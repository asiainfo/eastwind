package eastwind.io3;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.UUID;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eastwind.io3.model.Host;
import eastwind.io3.model.Shake;
import eastwind.io3.serializer.SerializerFactory;
import eastwind.io3.serializer.SerializerFactoryHolder;
import eastwind.io3.support.MillisX10Sequence;
import eastwind.io3.support.NamedThreadFactory;
import eastwind.io3.transport.ServerRepository;
import eastwind.io3.transport.TransportFactory;

public class EastwindClient {

	protected static Logger logger = LoggerFactory.getLogger(EastwindClient.class);

	protected String group;
	protected String uuid;
	protected Shake shake;

	private Bootstrap bootstrap = new Bootstrap();

	protected Sequence sequence = new MillisX10Sequence();
	protected TransmitSustainer transmitSustainer = new TransmitSustainer();
	protected ServerConfigurer serverConfigurer = new ServerConfigurer();
	protected TransportFactory transportFactory;
	protected ServerRepository serverRepository;
	protected InvokerFactory invokerFactory;
	private ClientFrameworkHandler clientFrameworkHandler;
	private ClientBusinessHandler clientBusinessHandler;

	protected int threads = 256;
	protected ThreadPoolExecutor executor;
	private AtomicBoolean started = new AtomicBoolean(false);

	protected SerializerFactoryHolder serializerFactoryHolder = new SerializerFactoryHolder();
	
	public EastwindClient(String group) {
		this.group = group;
		this.uuid = UUID.randomUUID().toString();
		this.shake = new Shake(group, this.uuid);

		transportFactory = new TransportFactory(bootstrap, shake, sequence, transmitSustainer);
		serverRepository = new ServerRepository(transportFactory);
		clientFrameworkHandler = new ClientFrameworkHandler(shake, transmitSustainer, transportFactory,
				serverRepository);
		clientBusinessHandler = new ClientBusinessHandler(transmitSustainer, executor);

		executor = new ThreadPoolExecutor(4, threads, 6, TimeUnit.MINUTES, new SynchronousQueue<Runnable>(),
				new NamedThreadFactory("message-executor"));
		invokerFactory = new InvokerFactory(serverConfigurer, serverRepository);
	}

	public void start() {
		if (!started.get() && started.compareAndSet(false, true)) {
			bootstrap.group(new NioEventLoopGroup(2)).channel(NioSocketChannel.class);
			bootstrap.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel sc) throws Exception {
					sc.pipeline().addLast("codec", new ObjectCodec());
					sc.pipeline().addLast("headedObjectCodec", new HeadedObjectCodec());
					sc.pipeline().addLast("clientObjectHandler", clientFrameworkHandler);
					sc.pipeline().addLast("clientBusinessHandler", clientBusinessHandler);
				}
			});
		}
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	public void addServer(String group, Host host) {
		serverConfigurer.addHost(group, host);
	}

	public <T> T createInvokerOnBinary(String group, Class<T> interf) {
		checkStart();
		return invokerFactory.getInvoker(group, interf);
	}

	public <T> void createInvokerOnJson(String group, String name, Class<T> resultType) {
	}

	public void setBinarySerializerFactory(SerializerFactory serializerFactory) {
		serializerFactoryHolder.setBinarySerializerFactory(serializerFactory);
	}
	
	public void setJsonSerializerFactory(SerializerFactory serializerFactory) {
		serializerFactoryHolder.setJsonSerializerFactory(serializerFactory);
	}
	
	static class JsonInvoker {

		private String group;
		private String name;

	}

	private void checkStart() {
		start();
	}

}
