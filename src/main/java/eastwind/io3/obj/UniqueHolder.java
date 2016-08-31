package eastwind.io3.obj;

public class UniqueHolder implements Unique {

	private long id;
	private Object obj;

	public static UniqueHolder hold(Object obj) {
		UniqueHolder holder = new UniqueHolder();
		holder.setObj(obj);
		return holder;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public void setId(long id) {
		this.id = id;
	}

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}
}
