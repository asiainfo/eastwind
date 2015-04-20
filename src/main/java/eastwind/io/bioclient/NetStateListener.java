package eastwind.io.bioclient;

import java.net.SocketAddress;

public abstract class NetStateListener {

	public boolean oneOff() {
		return false;
	}

	public NetState ExecuteIfOnState() {
		return null;
	}

	public abstract void stateChanged(SocketAddress socketAddress, NetState netState);

}
