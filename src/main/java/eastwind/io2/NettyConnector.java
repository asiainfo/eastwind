package eastwind.io2;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public abstract class NettyConnector extends AbstractConnector {

	private Bootstrap bootstrap = new Bootstrap();
	private ServerBootstrap serverBootstrap = new ServerBootstrap();
	private NioEventLoopGroup masterGroup;
	private NioEventLoopGroup workerGroup;

	public NettyConnector(int masterThreads, int workerThreads) {
		super(masterThreads, workerThreads);
		this.masterGroup = new NioEventLoopGroup(masterThreads);
		this.workerGroup = new NioEventLoopGroup(workerThreads);

		bootstrap.channel(NioSocketChannel.class);
		bootstrap.group(workerGroup);
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel sc) throws Exception {
				ChannelPipeline pipeline = sc.pipeline();
				pipeline.addLast(new NetworkTrafficCodec(serializerFactoryHolder));
				pipeline.addLast(new TransportInboundHandler());
			}
		});
		
		serverBootstrap.channel(NioServerSocketChannel.class).option(ChannelOption.SO_REUSEADDR, true);
		serverBootstrap.group(masterGroup, workerGroup);
		serverBootstrap.childHandler(new ChannelInitializeHandler(serializerFactoryHolder));
	}
	
	@Override
	public AcceptableTransport accept(InetSocketAddress localAddress) {
		ChannelFuture cf = serverBootstrap.bind(localAddress);
		return new AcceptableTransport(cf);
	}

	protected abstract void initServerSocketChannel(SocketChannel sc);
	
	@Override
	public ConnectedTransport connect(String group, SocketAddress remoteAddress) {
		ChannelFuture cf = bootstrap.connect(remoteAddress);
		return new ConnectedTransport(group, cf);
	}
	
}