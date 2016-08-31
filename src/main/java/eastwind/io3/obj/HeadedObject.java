package eastwind.io3.obj;

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

	public void setTh(Throwable th) {
		this.header.setSize((byte) 1);
		this.obj = th;
	}
	
	public void setObj(Object obj) {
		this.header.setSize((byte) 1);
		this.obj = obj;
	}

	public void setObjs(Object[] objs) {
		this.header.setSize((byte) objs.length);
		this.obj = objs;
	}
}
