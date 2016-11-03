package eastwind.io.transport;

public interface LoaderListener {

	void ready();
	
	void add(Node node, int mod, int preMod);
	
	void remove(Node node, int mod, int preMod);
}
