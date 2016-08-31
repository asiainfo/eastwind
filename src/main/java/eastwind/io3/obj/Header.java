package eastwind.io3.obj;

public class Header {

	public static final byte MESSAGE = 1;
	public static final byte REQUEST = 2;
	public static final byte RESPONSE = 3;
	
	private long id;
	private byte model;
	private String namespace;
	private byte size;
	private boolean th;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public byte getModel() {
		return model;
	}

	public void setModel(byte model) {
		this.model = model;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public byte getSize() {
		return size;
	}

	public void setSize(byte size) {
		this.size = size;
	}

	public boolean isTh() {
		return th;
	}

	public void setTh(boolean th) {
		this.th = th;
	}

}
