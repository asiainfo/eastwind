package eastwind.io2;

import com.esotericsoftware.kryo.serializers.FieldSerializer.Optional;

import eastwind.io.model.Unique;

public class Response extends NetworkTraffic implements Unique, Headed {

	private long id;
	private boolean failed;
	
	@Optional("serializer")
	private String serializer;
	@Optional("th")
	private Throwable th;
	@Optional("result")
	private Object result;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getSerializer() {
		return serializer;
	}

	public void setSerializer(String serializer) {
		this.serializer = serializer;
	}

	public boolean isFailed() {
		return failed;
	}

	public void setFailed(boolean failed) {
		this.failed = failed;
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

	@Override
	public Object getData() {
		return failed ? th : result;
	}

	@Override
	public int getDataLength() {
		return 1;
	}

	@Override
	public void setData(Object data) {
		if (failed) {
			this.th = (Throwable) data;
		} else {
			this.result = data;
		}
	}
}
