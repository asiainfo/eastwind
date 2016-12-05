package eastwind.io;

import java.util.List;

import com.google.common.collect.Lists;

public class ProviderInstance {

	private String alias;
	private Object obj;
	private List<Class<?>> interfs;
	private List<MethodHandler> handlers = Lists.newArrayList();

	public ProviderInstance(String alias, Object obj, List<Class<?>> interfs) {
		this.alias = alias;
		this.obj = obj;
		this.interfs = interfs;
	}

	public String getAlias() {
		return alias;
	}

	public Object getObj() {
		return obj;
	}

	public List<Class<?>> getInterfs() {
		return interfs;
	}

	public List<MethodHandler> getHandlers() {
		return handlers;
	}

}
