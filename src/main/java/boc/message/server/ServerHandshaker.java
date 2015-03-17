package boc.message.server;

import java.util.Map;

public abstract class ServerHandshaker {

	public boolean isMultiStep() {
		return false;
	}
	
	public abstract void prepare(Map<String, Object> out);

	public void handshake(Map<String, Object> in, Map<String, Object> out) {
		
	}

}
