package eastwind.io.nioclient;

import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;

// interface简称
public class InterfAb {

	private String uuid;
	private ConcurrentMap<String, String> interfIds = Maps.newConcurrentMap();

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
