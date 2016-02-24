package eastwind.io2.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eastwind.io.test.HelloImpl;
import eastwind.io2.MessageListener;
import eastwind.io2.ObjectCodec;
import eastwind.io2.ObjectHandlerRegistry;
import eastwind.io2.ObjectInboundHandler;

public class Server {

	private static Logger logger = LoggerFactory.getLogger(Server.class);

	private ServerBootstrap serverBootstrap = new ServerBootstrap();
	private ObjectHandlerRegistry objectHandlerRegistry = new ObjectHandlerRegistry();
	private String ip = "127.0.0.1";
	private int port = 12468;

	private int parentThreads = 1;
	private int childThreads = 0;

	public void start() {
		serverBootstrap.group(new NioEventLoopGroup(parentThreads), new NioEventLoopGroup(childThreads));
		serverBootstrap.channel(NioServerSocketChannel.class);
		serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel sc) throws Exception {
				sc.pipeline().addLast("codec", new ObjectCodec());
				sc.pipeline().addLast("handshakeHandler", new ServerHandshakeHandler());
				sc.pipeline().addLast("objectHandler", new ObjectInboundHandler(null, objectHandlerRegistry));
			}
		});
		serverBootstrap.option(ChannelOption.SO_REUSEADDR, true);
		serverBootstrap.bind(12468).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				logger.info("{}:{} started", ip, port);
			}
		});
	}

	public void registerRpcHandler(Object instance) {
		objectHandlerRegistry.registerRpcHandler(instance);
	}

	public void registerMessageListener(MessageListener<?> messageListener) {
		objectHandlerRegistry.registerMessageListener(messageListener);
	}

	public static void main(String[] args) throws IOException {
		Server server = new Server();
		server.registerMessageListener(new MessageListener<String>() {
			@Override
			public Object onMessage(String message) {
				System.out.println("-------" + message);
				return 1;
			}
		});
		server.registerRpcHandler(new HelloImpl());
		server.start();
		System.in.read();
	}
}
