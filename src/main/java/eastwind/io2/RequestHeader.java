package eastwind.io2;


public class RequestHeader {

	public static final byte MESSAGE = 0;
	public static final byte RPC = 1;
	
	private long id;
	private byte model;
	private String namespace;
	private int[] paramLens;
	
	public static boolean isMessage(RequestHeader requestHeader) {
		return requestHeader.model == MESSAGE;
	}
	
	public static boolean isRpc(RequestHeader requestHeader) {
		return requestHeader.model == RPC;
	}
	
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

	public int[] getParamLens() {
		return paramLens;
	}

	public void setParamLens(int[] paramLens) {
		this.paramLens = paramLens;
	}

}
