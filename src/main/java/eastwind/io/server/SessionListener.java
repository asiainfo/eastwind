package eastwind.io.server;

import eastwind.io.Session;

public interface SessionListener {

	public void created(Session session);

	public void recreated(Session session);
	
	public void suspended(Session session);
	
	public void destroyed(Session session);
}
