package eastwind.io3;


public class Application implements Group {

	protected String uuid;
	protected String group;

	public Application(String group) {
		this.group = group;
	}

	public String getUuid() {
		return uuid;
	}

	@Override
	public String getGroup() {
		return group;
	}
	
}
