package eastwind.io;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
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

import eastwind.io.invocation.SmartInvocationHandler;
import eastwind.io.invocation.SmartInvoker;
import eastwind.io.model.Shake;
import eastwind.io.serializer.JsonSerializerFactory;
import eastwind.io.serializer.KryoSerializerFactory;
import eastwind.io.serializer.SerializerFactory;
import eastwind.io.serializer.SerializerFactoryHolder;
import eastwind.io.support.MillisX10Sequence;
import eastwind.io.support.NamedThreadFactory;
import eastwind.io.transport.ServerRepository;
import eastwind.io.transport.TransportFactory;

public class EastWindClient {

	protected static Logger logger = LoggerFactory.getLogger(EastWindClient.class);

	protected String group;
	protected String uuid;
	protected String version;
	protected Shake shake;

	private Bootstrap bootstrap = new Bootstrap();

	protected Sequence sequence = new MillisX10Sequence();
	protected TransmitSustainer transmitSustainer = new TransmitSustainer();
	protected TransportFactory transportFactory;
	protected ServerRepository serverRepository;
	protected InvokerFactory invokerFactory;
	private ClientFrameworkHandler clientFrameworkHandler;
	private ClientBusinessHandler clientBusinessHandler;

	private int connectionsPerHost = 2;
	protected int threads = 256;
	protected ThreadPoolExecutor executor;
	private AtomicBoolean started = new AtomicBoolean(false);

	protected SerializerFactoryHolder serializerFactoryHolder = new SerializerFactoryHolder();

	public EastWindClient(String group) {
		this.group = group;
		this.uuid = UUID.randomUUID().toString();
		this.shake = new Shake(group, this.uuid);

		transportFactory = new TransportFactory(bootstrap, shake, sequence, transmitSustainer);
		serverRepository = new ServerRepository(transportFactory, connectionsPerHost);
		clientFrameworkHandler = new ClientFrameworkHandler(shake, transmitSustainer, transportFactory,
				serverRepository);
		clientBusinessHandler = new ClientBusinessHandler(transmitSustainer, executor);

		executor = new ThreadPoolExecutor(4, threads, 6, TimeUnit.MINUTES, new SynchronousQueue<Runnable>(),
				new NamedThreadFactory("message-executor"));
		invokerFactory = new InvokerFactory(serverRepository);
	}

	public void start() {
		if (!started.get() && started.compareAndSet(false, true)) {
			initSerializer();
			bootstrap.group(new NioEventLoopGroup(2)).channel(NioSocketChannel.class);
			bootstrap.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel sc) throws Exception {
					ChannelPipeline pipeline = sc.pipeline();

					pipeline.addLast("objectCodec", new ObjectCodec(serializerFactoryHolder, null, transmitSustainer));
					pipeline.addLast("headedObjectCodec", new HeadedObjectCodec());
					pipeline.addLast("clientFrameworkHandler", clientFrameworkHandler);
					pipeline.addLast("clientBusinessHandler", clientBusinessHandler);
				}
			});
		}
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setConnectionsPerHost(int connectionsPerHost) {
		this.connectionsPerHost = connectionsPerHost;
	}

	public <T> T createProxyInvoker(String group, Class<T> interf) {
		checkStart();
		return invokerFactory.getInvoker(group, interf);
	}

	public <T> SmartInvoker<T> createSmartInvoker(String group, String name, Class<T> returnType) {
		checkStart();
		SmartInvocationHandler handler = new SmartInvocationHandler(group, serverRepository, returnType);
		return new SmartInvoker<T>(group, name, returnType, handler);
	}

	public void setProxySerializerFactory(SerializerFactory serializerFactory) {
		serializerFactoryHolder.setProxySerializerFactory(serializerFactory);
	}

	public void setSmartSerializerFactory(SerializerFactory serializerFactory) {
		serializerFactoryHolder.setSmartSerializerFactory(serializerFactory);
	}

	protected void initSerializer() {
		if (serializerFactoryHolder.getProxySerializer() == null) {
			serializerFactoryHolder.setProxySerializerFactory(new KryoSerializerFactory());
		}
		if (serializerFactoryHolder.getSmartSerializer() == null) {
			serializerFactoryHolder.setSmartSerializerFactory(new JsonSerializerFactory());
		}
	}

	private void checkStart() {
		start();
	}

}
