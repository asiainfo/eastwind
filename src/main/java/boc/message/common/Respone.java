package boc.message.common;

public class Respone<R> {

	private long id;
	private R result;
	private Throwable th;

	public Respone() {

	}

	public Respone(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public R getResult() {
		return result;
	}

	public void setResult(R result) {
		this.result = result;
	}

	public Throwable getTh() {
		return th;
	}

	public void setTh(Throwable th) {
		this.th = th;
	}
}
