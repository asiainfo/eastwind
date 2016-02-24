package eastwind.io2.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;
import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eastwind.io.common.Host;
import eastwind.io2.HeadedObject;
import eastwind.io2.Header;
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
	private OutboundChannelManager outboundChannelManager = new OutboundChannelManager(bootstrap,
			applicationConfigManager);

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
				new RpcInvocationHandler(app, rpcContextPool, timeSequence10, outboundChannelManager));
		return (T) obj;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		String name = "test-client";
		String target = "test-server";
		ApplicationConfig config = new ApplicationConfig(target);
		Host host = new Host("127.0.0.1", 12468);
		config.addHost(new Host("127.0.0.1", 12468));
		Client client = new Client(name);
		client.addApplicationConfig(config);
		client.start();
		OutboundChannel oc = client.outboundChannelManager.getOutboundChannel(target, host);
		Channel channel = oc.getHandshakePromise().sync().channel();

		HeadedObject ho = new HeadedObject();
		Header header = new Header();
		header.setId(123);
		header.setModel((byte) 1);
		header.setNamespace("test");
		ho.setHeader(header);
//		ho.setObjs(new Object[] { "abc" });
		ho.setObjs(new Object[] { "abc", 123 });

		channel.writeAndFlush(ho);

		// Hello hello = client.createRpcInvoker(target, Hello.class);
		// System.out.println(hello.hello("rpc"));
		System.in.read();
	}
}
