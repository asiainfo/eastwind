package eastwind.io.common;

public abstract class InformHandler {
	
	public int type() {
		return -1;
	}
	
	public abstract void handle(Push inform);
}
