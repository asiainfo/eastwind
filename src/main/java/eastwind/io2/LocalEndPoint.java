package eastwind.io2;

import java.net.InetSocketAddress;


public interface LocalEndPoint extends EventEndPoint {

	void registerProvider(Object provider);
	
	void setLocalAddress(InetSocketAddress localAddress);
	
	void setMasterThreads(int masterThreads);

	void setWorkerThreads(int workerThreads);
}
