package eastwind.io2;

import java.lang.reflect.Method;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public abstract class AbstractEndPoint implements EndPoint {

	protected static Logger logger = LoggerFactory.getLogger(EndPoint.class);
	
	public static final int DEFAULT_WEIGHT = 10;
	
	protected String uuid;
	protected String group;
	protected String tag;
	protected String version;
	protected int weight = DEFAULT_WEIGHT;

	protected AbstractEndPoint(String uuid, String group, String tag, String version) {
		this.uuid = uuid;
		this.group = group;
		this.tag = tag;
		this.version = version;
	}

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

	protected void setWeight(int weight) {
		this.weight = weight;
	}

	protected Shake createShake() {
		Shake shake = new Shake();
		shake.setMyGroup(group);
		shake.setMyUuid(uuid);
		return shake;
	}
}
