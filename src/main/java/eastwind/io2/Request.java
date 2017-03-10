package eastwind.io2;

import com.esotericsoftware.kryo.serializers.FieldSerializer.Optional;

import eastwind.io.model.Unique;

public class Request extends NetworkTraffic implements Unique, Headed {

	private long id;
	private String name;
	private boolean internal;
	private String serializer;
	private int dataLength;
	
	@Optional("X")
	private Object data;

	@Override
	public long getId() {
		return id;
	}

	@Override
	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isInternal() {
		return internal;
	}

	public void setInternal(boolean internal) {
		this.internal = internal;
	}

	public String getSerializer() {
		return serializer;
	}

	public void setSerializer(String serializer) {
		this.serializer = serializer;
	}

	@Override
	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	@Override
	public int getDataLength() {
		return dataLength;
	}

	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}

}