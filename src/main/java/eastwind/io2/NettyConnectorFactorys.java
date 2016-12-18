package eastwind.io2;

public class NettyConnectorFactorys {

	public static NettyConnectorFactory serverConnectorFactory() {
		return new NettyServerConnectorFactory();
	}

	public static NettyConnectorFactory clientConnectorFactory() {
		return new NettyClientConnectorFactory();
	}

	static class NettyServerConnectorFactory implements NettyConnectorFactory {

		@Override
		public NettyConnector createConnector(int masterThreads, int workerThreads) {
			return new NettyServerConnector(masterThreads, workerThreads);
		}
	}

	static class NettyClientConnectorFactory implements NettyConnectorFactory {

		@Override
		public NettyConnector createConnector(int masterThreads, int workerThreads) {
			return new NettyClientConnector(masterThreads, workerThreads);
		}
	}
}
