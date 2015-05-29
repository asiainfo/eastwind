package eastwind.io.common;

public abstract class MessagingHandler {
	
	public abstract int type();
	
	public abstract void handle(Messaging messaging);
}
