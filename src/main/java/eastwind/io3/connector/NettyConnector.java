package eastwind.io3.connector;

import java.net.InetSocketAddress;

import eastwind.io3.codex.HandlerInitializerSelector;
import eastwind.io3.codex.HeadHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyConnector {

	private ServerBootstrap serverBootstrap = new ServerBootstrap();
	private Bootstrap bootstrap = new Bootstrap();
	private InetSocketAddress localAddress;
	private InputChannelHanderAdapter inputChannelHanderAdapter = new InputChannelHanderAdapter();

	public NettyConnector(HandlerInitializerSelector handlerInitializerSelector) {
		NioEventLoopGroup group = new NioEventLoopGroup();

		ChannelInitializer<Channel> sci = new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
				ch.pipeline().addLast(new InputHeadHandler(handlerInitializerSelector, inputChannelHanderAdapter));
			}
		};

		serverBootstrap.channel(NioServerSocketChannel.class);
		serverBootstrap.group(new NioEventLoopGroup(1), group);
		serverBootstrap.childHandler(sci);
		serverBootstrap.option(ChannelOption.SO_REUSEADDR, true);
		serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);

		ChannelInitializer<Channel> ci = new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
				ch.pipeline().addLast(new HeadHandler(handlerInitializerSelector));
			}
		};
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.group(group);
		bootstrap.handler(ci);
	}

	public InetSocketAddress getLocalAddress() {
		return localAddress;
	}

	public void setLocalAddress(InetSocketAddress localAddress) {
		this.localAddress = localAddress;
	}

	public MasterChannel open() {
		if (localAddress == null) {
			localAddress = new InetSocketAddress("0.0.0.0", 12468);
		}
		return new MasterChannel(serverBootstrap.bind(localAddress));
	}

	public OutputChannel connect(InetSocketAddress remoteAddress) {
		return new OutputChannel(bootstrap.connect(remoteAddress));
	}

	public void connected(InputChannelHandler handler) {
		inputChannelHanderAdapter.setHandler(handler);
	}

	static class InputChannelHanderAdapter implements InputChannelHandler {

		private InputChannelHandler handler;

		@Override
		public void handle(InputChannel event) {
			handler.handle(event);
		}

		public void setHandler(InputChannelHandler handler) {
			this.handler = handler;
		}
	}
}
