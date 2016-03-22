package eastwind.io3;

public class Response {

	private long id;
	private Throwable th;
	private Object result;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
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
