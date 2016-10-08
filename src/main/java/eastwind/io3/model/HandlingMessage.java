package eastwind.io3.model;


public class HandlingMessage implements Unique {

	private long id;
	private long time = System.currentTimeMillis();
	private boolean done;
	private boolean th;
	private Object response;
	
	@Override
	public long getId() {
		return id;
	}

	@Override
	public void setId(long id) {
		this.id = id;
	}

	public long getTime() {
		return time;
	}

	public Object getResponse() {
		return response;
	}

	public void setResponse(Object response) {
		this.response = response;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public boolean isTh() {
		return th;
	}

	public void setTh(boolean th) {
		this.th = th;
	}
	
}
