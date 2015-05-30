package eastwind.io.common;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

// interface简称
public class InterfAb {

	private String uuid;
	private ConcurrentMap<String, String> interfIds = new ConcurrentHashMap<String, String>();

	public void ackUuid(String uuid) {
		if (!uuid.equals(this.uuid)) {
			this.interfIds.clear();
			this.uuid = uuid;
		}
	}

	public String getInterfId(String interf) {
		return interfIds.get(interf);
	}

	public void setInterfId(String interf, String id) {
		this.interfIds.put(interf, id);
	}
}
