package eastwind.io2;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;


public class EndPointGroup {

	private String group;
	private Map<SocketAddress, RemoteEndPoint> addressEndPoints = new HashMap<SocketAddress, RemoteEndPoint>();
	private Map<String, RemoteEndPoint> uuidEndPoints = new HashMap<String, RemoteEndPoint>();
	
	public EndPointGroup(String group) {
		super();
		this.group = group;
	}
	
	public RemoteEndPoint getEndPoint(String uuid) {
		return uuidEndPoints.get(uuid);
	}
	
	public RemoteEndPoint createEndPoint(String uuid) {
		return new RemoteEndPoint(uuid, group, null, null);
	}
	
	protected String getGroup() {
		return group;
	}
}
