package eastwind.io2;

public interface SessionListener {

	public void created(Session session);

	public void reactived(Session session);
	
	public void suspended(Session session);
	
	public void destroyed(Session session);
}
