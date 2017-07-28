package eastwind.io3;

public class Peer extends AttributeMap {

	public static final int DEFAULT_WEIGHT = 10;

	protected String uuid;
	protected String group;
	protected String version;
	protected int weight = DEFAULT_WEIGHT;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}
}
