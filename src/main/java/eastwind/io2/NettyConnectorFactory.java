package eastwind.io2;

public interface NettyConnectorFactory {

	NettyConnector createConnector(int masterThreads, int workerThreads);
	
}
