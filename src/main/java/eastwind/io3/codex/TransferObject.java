package eastwind.io3.codex;

public class TransferObject {

	private long beginTime;
	private long allTime;
	private long handleTime;
	private int bytes;
	private String handler;
	private Object content;

	public long getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(long beginTime) {
		this.beginTime = beginTime;
	}

	public long getAllTime() {
		return allTime;
	}

	public void setAllTime(long allTime) {
		this.allTime = allTime;
	}

	public long getHandleTime() {
		return handleTime;
	}

	public void setHandleTime(long handleTime) {
		this.handleTime = handleTime;
	}

	public int getBytes() {
		return bytes;
	}

	public void setBytes(int bytes) {
		this.bytes = bytes;
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public Object getContent() {
		return content;
	}

	public void setContent(Object content) {
		this.content = content;
	}

}
