package eastwind.io2;

import com.esotericsoftware.kryo.serializers.FieldSerializer.Optional;

import eastwind.io.model.Unique;

public class Response extends NetworkTraffic implements Unique {

	private long id;
	private boolean failed;
	
	@Optional("th")
	private Throwable th;
	@Optional("result")
	private Object result;
	@Optional("serializer")
	private String serializer;

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
}
