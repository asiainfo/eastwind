package eastwind.io.common;

import java.util.Map;

public class Handshake {

	public int step;

	private Map<String, Object> attributes;
	
	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}
}
