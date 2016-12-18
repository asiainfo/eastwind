package eastwind.io2;

import java.lang.reflect.Method;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import eastwind.io.model.ProviderMetaData;

public abstract class AbstractEndPoint implements EndPoint {

	protected static Logger logger = LoggerFactory.getLogger(EndPoint.class);
	
	protected String uuid;
	protected String group;
	protected String tag;
	protected String version;
	protected int weight;

	protected Map<Method, ProviderMetaData> methodMetaDatas = Maps.newHashMap();
	protected Map<String, ProviderMetaData> namedMetaDatas = Maps.newHashMap();

	protected AbstractEndPoint(String uuid, String group, String tag, String version, int weight) {
		this.uuid = uuid;
		this.group = group;
		this.tag = tag;
		this.version = version;
		this.weight = weight;
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
	
	@Override
	public void addProvider(ProviderMetaData meta) {
		methodMetaDatas.put(meta.getMethod(), meta);
		namedMetaDatas.put(meta.getName(), meta);
	}

	@Override
	public ProviderMetaData getProvider(Method method) {
		return methodMetaDatas.get(method);
	}

	@Override
	public ProviderMetaData getProvider(String name) {
		return namedMetaDatas.get(name);
	}

}
