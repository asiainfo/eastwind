package eastwind.io3;

public class Handshake implements FrameworkObject {

	private String group;
	private String myUuid;
	private String yourUuid;

	public Handshake() {
	}

	public Handshake(String group, String myUuid, String yourUuid) {
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

}
