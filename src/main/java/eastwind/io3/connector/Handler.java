package eastwind.io3.connector;

public interface Handler<T> {

	void handle(T event);
	
}
