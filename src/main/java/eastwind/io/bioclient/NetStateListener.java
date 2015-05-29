package eastwind.io.bioclient;

import java.net.SocketAddress;

public abstract class NetStateListener {

	public abstract void stateChanged(SocketAddress socketAddress, NetState netState);

}