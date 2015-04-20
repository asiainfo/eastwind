package boc.message.common;

public class Push {

	private int type;
	private long sequentId;
	private Object data;

	public Push() {

	}

	public Push(int type, long sequentId, Object data) {
		this.type = type;
		this.sequentId = sequentId;
		this.data = data;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public long getSequentId() {
		return sequentId;
	}

	public void setSequentId(long sequentId) {
		this.sequentId = sequentId;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

}
