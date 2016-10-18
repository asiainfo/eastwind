package eastwind.io.model;


public class Response implements Unique, BusinessObject {

	private long id;
	private boolean binary;
	private Throwable th;
	private Object result;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public boolean isBinary() {
		return binary;
	}

	public void setBinary(boolean binary) {
		this.binary = binary;
	}

	public Throwable getTh() {
		return th;
	}

	public void setTh(Throwable th) {
		this.th = th;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}
}
