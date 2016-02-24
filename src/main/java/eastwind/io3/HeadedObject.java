package eastwind.io3;

public class HeadedObject {

	private Header header;
	private Object obj;

	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.header.setSize(1);
		this.obj = obj;
	}

	public void setObjs(Object[] objs) {
		this.header.setSize(objs.length);
		this.obj = objs;
	}
}
