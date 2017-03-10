package eastwind.io2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPeer implements Peer {

	protected static Logger logger = LoggerFactory.getLogger(Peer.class);
	
	public static final int DEFAULT_WEIGHT = 10;
	
	protected String uuid;
	protected String group;
	protected String tag;
	protected String version;
	protected int weight = DEFAULT_WEIGHT;

	@Override
	public String getUuid() {
		return uuid;
	}

	@Override
	public String getGroup() {
		return group;
	}

	@Override
	public String getTag() {
		return tag;
	}

	@Override
	public int getWeight() {
		return weight;
	}

	@Override
	public String getVersion() {
		return version;
	}

	protected Shake createShake() {
		Shake shake = new Shake();
		shake.setMyGroup(group);
		shake.setMyUuid(uuid);
		return shake;
	}
}
