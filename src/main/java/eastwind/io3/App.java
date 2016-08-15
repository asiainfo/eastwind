package eastwind.io3;


public class App implements Group {

	protected String uuid;
	protected String group;

	public App(String group) {
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
