package eastwind.io2;

import java.util.Map;

import eastwind.io.common.Host;

public class Handshake {

	private String myUuid;
	private String yourUuid;
	private String app;
	// 作为服务端时的地址和端口
	private Host host;
	private boolean success;
	private Map<Object, Object> properties;

	public String getMyUuid() {
		return myUuid;
	}

	public void setMyUuid(String myUuid) {
		this.myUuid = myUuid;
	}

	public String getYourUuid() {
		return yourUuid;
	}

	public void setYourUuid(String yourUuid) {
		this.yourUuid = yourUuid;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public Host getHost() {
		return host;
	}

	public void setHost(Host host) {
		this.host = host;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public Map<Object, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<Object, Object> properties) {
		this.properties = properties;
	}

}
