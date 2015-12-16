package eastwind.io2.client;

import java.util.Map;

import eastwind.io.common.Host;

public abstract class ClientHandshake {

	protected abstract Map<Object, Object> prepare();

	protected abstract void complete(boolean result, String remoteApp, Host remoteHost, Object property);
}
