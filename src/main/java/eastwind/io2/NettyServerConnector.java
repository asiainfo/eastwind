package eastwind.io2;

import io.netty.channel.socket.SocketChannel;

public class NettyServerConnector extends NettyConnector {

	public NettyServerConnector(int masterThreads, int workerThreads) {
		super(masterThreads, workerThreads);
	}

	@Override
	protected void initServerSocketChannel(SocketChannel sc) {
		
	}

}
