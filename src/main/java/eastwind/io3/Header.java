package eastwind.io3;

public class Header {

	public static final byte MESSAGE = 1;
	public static final byte REQUEST = 2;
	public static final byte RESPONSE = 3;
	
	private long id;
	private byte model;
	private String namespace;
	private int size;
	
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

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

}
