package eastwind.io2;

import java.lang.reflect.Method;

import eastwind.io.model.ProviderMetaData;

public interface EndPoint {

	String getUuid();
	
	String getGroup();
	
	String getTag();
	
	String getVersion();
	
	int getWeight();
	
	void attach(Transport transport);
	
	void addProvider(ProviderMetaData meta);
	
	ProviderMetaData getProvider(Method method);
	
	ProviderMetaData getProvider(String name);
}