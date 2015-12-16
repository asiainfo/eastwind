package eastwind.io2;

import java.util.Map;

import eastwind.io.common.Host;

public class Handshake {

	private String uuid;
	private String preUuid;
	private String app;
	// 作为服务端时的地址和端口
	private Host host;
	private boolean success;
	private Map<Object, Object> properties;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getPreUuid() {
		return preUuid;
	}

	public void setPreUuid(String preUuid) {
		this.preUuid = preUuid;
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
