package eastwind.io2;

import java.net.InetSocketAddress;


public interface LocalEndPoint extends Peer {

	void registerProvider(Object provider);
	
	InetSocketAddress getLocalAddress();
	
	void setLocalAddress(InetSocketAddress localAddress);
	
	void setMasterThreads(int masterThreads);

	void setWorkerThreads(int workerThreads);
}
