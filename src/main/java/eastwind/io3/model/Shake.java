package eastwind.io3.model;


public class Shake implements FrameworkObject {

	private String group;
	private String uuid;
	private String myUuid;
	private String yourUuid;

	public Shake() {
	}

	public Shake(String group, String uuid) {
		this.group = group;
		this.uuid = uuid;
	}

	public Shake(String group, String myUuid, String yourUuid) {
		this.group = group;
		this.myUuid = myUuid;
		this.yourUuid = yourUuid;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getMyUuid() {
		return myUuid;
	}

	public void setMyUuid(String myUuid) {
		this.myUuid = myUuid;
	}

	public String getYourUuid() {
		return yourUuid;
	}

	public void setYourUuid(String yourUuid) {
		this.yourUuid = yourUuid;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

}
