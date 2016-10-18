package eastwind.io;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.lang.reflect.Type;
import java.util.UUID;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eastwind.io.invocation.JsonInvocationHandler;
import eastwind.io.invocation.JsonInvoker;
import eastwind.io.model.Host;
import eastwind.io.model.Shake;
import eastwind.io.serializer.JsonSerializerFactory;
import eastwind.io.serializer.KryoSerializerFactory;
import eastwind.io.serializer.SerializerFactory;
import eastwind.io.serializer.SerializerFactoryHolder;
import eastwind.io.support.MillisX10Sequence;
import eastwind.io.support.NamedThreadFactory;
import eastwind.io.transport.ServerRepository;
import eastwind.io.transport.TransportFactory;

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
			initSerializer();
			bootstrap.group(new NioEventLoopGroup(2)).channel(NioSocketChannel.class);
			bootstrap.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel sc) throws Exception {
					sc.pipeline().addLast("objectCodec",
							new ObjectCodec(serializerFactoryHolder, null, transmitSustainer));
					sc.pipeline().addLast("headedObjectCodec", new HeadedObjectCodec());
					sc.pipeline().addLast("clientFrameworkHandler", clientFrameworkHandler);
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

	public <T> JsonInvoker<T> createInvokerOnJson(String group, String name, Class<T> returnType) {
		checkStart();
		JsonInvocationHandler handler = new JsonInvocationHandler(group, serverRepository, serverConfigurer, returnType);
		return new JsonInvoker<T>(group, name, returnType, handler);
	}

	public <T> void createInvokerOnJson(String group, String name, Type returnType) {
	}

	public void setBinarySerializerFactory(SerializerFactory serializerFactory) {
		serializerFactoryHolder.setBinarySerializerFactory(serializerFactory);
	}

	public void setJsonSerializerFactory(SerializerFactory serializerFactory) {
		serializerFactoryHolder.setJsonSerializerFactory(serializerFactory);
	}

	protected void initSerializer() {
		if (serializerFactoryHolder.getBinarySerializer() == null) {
			serializerFactoryHolder.setBinarySerializerFactory(new KryoSerializerFactory());
		}
		if (serializerFactoryHolder.getJsonSerializer() == null) {
			serializerFactoryHolder.setJsonSerializerFactory(new JsonSerializerFactory());
		}
	}
	
	private void checkStart() {
		start();
	}

}
