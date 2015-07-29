package eastwind.io.common;

public interface MessagingHandler {
	
	int type();
	
	void handle(Messaging messaging);
}
