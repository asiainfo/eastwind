package eastwind.io.transport;

import eastwind.io.model.Host;

public class Node {

	private String group;
	private String tag;
	private Host host;
	private int weight = 10;
	
	private int tmpWeight;
	private boolean frozen;
	
	public Node() {
	}

	public Node(Host host) {
		this.host = host;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public Host getHost() {
		return host;
	}

	public void setHost(Host host) {
		this.host = host;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int getTmpWeight() {
		return tmpWeight;
	}

	public void setTmpWeight(int tmpWeight) {
		this.tmpWeight = tmpWeight;
	}

	public boolean isFrozen() {
		return frozen;
	}

	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}

	@Override
	public int hashCode() {
		return host.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		return true;
	}
	
}
