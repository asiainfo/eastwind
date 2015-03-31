package boc.message.common;

public class Inform {

	private int type;
	private Object data;

	public Inform() {

	}

	public Inform(int type, Object data) {
		this.type = type;
		this.data = data;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

}
