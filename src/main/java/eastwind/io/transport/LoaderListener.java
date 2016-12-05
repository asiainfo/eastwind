package eastwind.io.transport;

public interface LoaderListener {

	void refresh();
	
	void add(Node node, int mod, int preMod);
	
	void remove(Node node, int mod, int preMod);
}
