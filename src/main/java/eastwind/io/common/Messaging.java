package eastwind.io.common;

public class Messaging {

	public static final int INTERF_ID = 11;
	
	private int type;
	private long id;
	private Object data;

	public Messaging() {

	}

	public Messaging(int type, long id, Object data) {
		this.type = type;
		this.id = id;
		this.data = data;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

}
