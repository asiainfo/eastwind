package boc.message.common;

public abstract class InformHandler {
	
	public int type() {
		return -1;
	}
	
	public abstract void handle(Inform inform);
}
