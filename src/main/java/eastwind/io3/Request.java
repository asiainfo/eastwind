package eastwind.io3;

public class Request implements Unique, Headed {

	private long id;
	private String namespace;
	private Object[] args;

	public Request() {
		
	}
	
	public Request(String namespace, Object[] args) {
		this.namespace = namespace;
		this.args = args;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

}
