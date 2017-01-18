package eastwind.io2;

import java.lang.reflect.Method;

public interface EndPoint {

	String getUuid();
	
	String getGroup();
	
	String getTag();
	
	String getVersion();
	
	int getWeight();
	
}