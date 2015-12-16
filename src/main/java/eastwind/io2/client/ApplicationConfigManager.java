package eastwind.io2.client;

import java.util.Map;

import com.google.common.collect.Maps;

public class ApplicationConfigManager {

	private Map<String, ApplicationConfig> configs = Maps.newHashMap();
	
	public void addConfig(ApplicationConfig config) {
		configs.put(config.getName(), config);
	}
	
}
