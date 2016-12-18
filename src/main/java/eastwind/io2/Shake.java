package eastwind.io2;

public class Shake extends NetworkTraffic {

	private String myUuid;
	private String myGroup;

	private String yourUuid;
	private String yourGroup;

	public String getMyUuid() {
		return myUuid;
	}

	public void setMyUuid(String myUuid) {
		this.myUuid = myUuid;
	}

	public String getMyGroup() {
		return myGroup;
	}

	public void setMyGroup(String myGroup) {
		this.myGroup = myGroup;
	}

	public String getYourUuid() {
		return yourUuid;
	}

	public void setYourUuid(String yourUuid) {
		this.yourUuid = yourUuid;
	}

	public String getYourGroup() {
		return yourGroup;
	}

	public void setYourGroup(String yourGroup) {
		this.yourGroup = yourGroup;
	}
}
