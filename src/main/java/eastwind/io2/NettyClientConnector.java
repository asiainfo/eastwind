package eastwind.io2;

import io.netty.channel.socket.SocketChannel;

public class NettyClientConnector extends NettyConnector {

	public NettyClientConnector(int masterThreads, int workerThreads) {
		super(masterThreads, workerThreads);
	}

	@Override
	protected void initServerSocketChannel(SocketChannel sc) {
		// TODO Auto-generated method stub
		
	}

}
