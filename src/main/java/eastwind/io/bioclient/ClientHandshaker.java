package eastwind.io.bioclient;

import java.util.Map;

public abstract class ClientHandshaker {

	public void prepare(EastWindClient client, Map<String, Object> in, Map<String, Object> out) {

	}

	public void handshakeComplete(Map<String, Object> in) {

	}
}
