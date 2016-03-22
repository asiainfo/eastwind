package eastwind.io3;

public abstract class GenericApplication implements Application {

	protected String uuid;
	protected String group;

	public GenericApplication(String group) {
		this.group = group;
	}

	public String getUuid() {
		return uuid;
	}

	public String getGroup() {
		return group;
	}

}
