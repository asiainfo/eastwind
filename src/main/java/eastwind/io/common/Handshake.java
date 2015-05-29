package eastwind.io.common;

import java.util.Map;

public class Handshake {

	private String app;
	private String uuid;
	public int step;

	private Map<String, Object> attributes;
	
	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}
}
