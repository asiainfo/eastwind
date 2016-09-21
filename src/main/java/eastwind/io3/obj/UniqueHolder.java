package eastwind.io3.obj;

public class UniqueHolder implements FrameworkObject, Unique {

	private long preId;
	private long id;
	private Object obj;

	public static UniqueHolder hold(Object obj) {
		UniqueHolder holder = new UniqueHolder();
		holder.setObj(obj);
		return holder;
	}

	public static UniqueHolder reply(UniqueHolder input, Object obj) {
		UniqueHolder reply = new UniqueHolder();
		reply.setPreId(input.getId());
		reply.setObj(obj);
		return reply;
	}
	
	@Override
	public long getId() {
		return id;
	}

	@Override
	public void setId(long id) {
		this.id = id;
	}

	public long getPreId() {
		return preId;
	}

	public void setPreId(long preId) {
		this.preId = preId;
	}

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}
}
