package eastwind.io.common;

public abstract class MessagingHandler {
	
	public int type() {
		return -1;
	}
	
	public abstract void handle(Messaging messaging);
}
