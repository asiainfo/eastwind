package eastwind.io3;

public abstract class GenericApplication implements Application {

	protected String uuid;
	protected String group;

	public String getUuid() {
		return uuid;
	}

	public String getGroup() {
		return group;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public void setGroup(String group) {
		this.group = group;
	}

}
