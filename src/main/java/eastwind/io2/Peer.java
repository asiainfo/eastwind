package eastwind.io2;


public interface Peer {

	String getUuid();
	
	String getGroup();
	
	String getTag();
	
	String getVersion();
	
	int getWeight();
	
}