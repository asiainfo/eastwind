package boc.message.common;

public abstract class NoticeHandler {
	
	public int type() {
		return -1;
	}
	
	public abstract void handle(Notice notice);
}
