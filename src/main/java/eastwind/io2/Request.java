package eastwind.io2;

import com.esotericsoftware.kryo.serializers.FieldSerializer.Optional;

import eastwind.io.model.Unique;

public class Request extends NetworkTraffic implements Unique {

	private long id;
	private String name;
	private boolean internal;
	private String serializer;
	private int argsLength;
	
	@Optional("args")
	private Object[] args;

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

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

	public int getArgsLength() {
		return argsLength;
	}

	public void setArgsLength(int argsLength) {
		this.argsLength = argsLength;
	}
}