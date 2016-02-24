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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eastwind.io2.ObjectCodec;
import eastwind.io2.ObjectHandlerRegistry;

public class EastwindFramework extends GenericApplication implements RegistrableApplication {

	private static Logger logger = LoggerFactory.getLogger(EastwindFramework.class);

	private Bootstrap bootstrap = new Bootstrap();
	private ServerBootstrap serverBootstrap = new ServerBootstrap();
	private ObjectHandlerRegistry objectHandlerRegistry = new ObjectHandlerRegistry();
	private int port = 12468;

	public void start() {
		bootstrap.group(new NioEventLoopGroup(2)).channel(NioSocketChannel.class);
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel sc) throws Exception {
				sc.pipeline().addLast("codec", new ObjectCodec());
			}
		});

		serverBootstrap.group(new NioEventLoopGroup(2), new NioEventLoopGroup());
		serverBootstrap.channel(NioServerSocketChannel.class);
		serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel sc) throws Exception {
				sc.pipeline().addLast("codec", new ObjectCodec());
			}
		});
		
		serverBootstrap.option(ChannelOption.SO_REUSEADDR, true);
		serverBootstrap.bind(port).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				logger.info("start at port:{}", port);
			}
		});
	}

	public static void main(String[] args) {

	}

}
