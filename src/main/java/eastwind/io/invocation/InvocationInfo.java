package eastwind.io.invocation;

public class InvocationInfo {

	private String group;
	private String version;
	private String ip;
	private String name;
	private Object[] args;
	
	public InvocationInfo(String group, Object[] args) {
		this.group = group;
		this.args = args;
	}

	public String getVersion() {
		return version;
	}

	public String getGroup() {
		return group;
	}

	public String getIp() {
		return ip;
	}

	public String getName() {
		return name;
	}

	public Object[] getArgs() {
		return args;
	}
}
