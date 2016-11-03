package eastwind.io.invocation;

public interface ServerSelector {

	public boolean hasNext();
	
	public Server next();
	
	public boolean exclusive();
	
	public boolean skippable();
	
}
