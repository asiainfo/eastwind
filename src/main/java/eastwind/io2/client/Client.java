package eastwind.io2.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;
import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eastwind.io.common.Host;
import eastwind.io.test.Hello;
import eastwind.io2.ObjectCodec;
import eastwind.io2.ObjectHandlerRegistry;
import eastwind.io2.ObjectInboundHandler;
import eastwind.io2.TimeSequence10;

public class Client {

	private static Logger logger = LoggerFactory.getLogger(Client.class);

	private String name;
	private Bootstrap bootstrap = new Bootstrap();
	private TimeSequence10 timeSequence10 = new TimeSequence10();
	private RpcContextPool rpcContextPool = new RpcContextPool();
	private ApplicationConfigManager applicationConfigManager = new ApplicationConfigManager();
	private ObjectHandlerRegistry objectHandlerRegistry = new ObjectHandlerRegistry();
	private ClientChannelManager clientChannelManager = new ClientChannelManager(bootstrap, applicationConfigManager);

	private int threads = 0;

	public Client(String name) {
		this.name = name;
	}

	public void start() {
		bootstrap.group(new NioEventLoopGroup(threads)).channel(NioSocketChannel.class);
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel sc) throws Exception {
				sc.pipeline().addLast("codec", new ObjectCodec());
				sc.pipeline().addLast("handshakeHandler", new ClientHandshakeHandler());
				sc.pipeline().addLast("objectHandler", new ObjectInboundHandler(rpcContextPool, objectHandlerRegistry));
			}
		});
	}

	public void addApplicationConfig(ApplicationConfig config) {
		this.applicationConfigManager.addConfig(config);
	}

	@SuppressWarnings("unchecked")
	public <T> T createRpcInvoker(String app, Class<T> interf) {
		Object obj = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[] { interf },
				new RpcInvocationHandler(app, rpcContextPool, timeSequence10, clientChannelManager));
		return (T) obj;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		String name = "test-client";
		String target = "test-server";
		ApplicationConfig config = new ApplicationConfig(target);
		config.addHost(new Host("127.0.0.1", 12468));
		Client client = new Client(name);
		client.addApplicationConfig(config);
		client.start();
		Hello hello = client.createRpcInvoker(target, Hello.class);
		System.out.println(hello.hello("rpc"));
		System.in.read();
	}
}
