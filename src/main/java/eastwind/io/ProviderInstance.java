package eastwind.io;

import java.util.List;

import com.google.common.collect.Lists;

public class ProviderInstance {

	private String namespace;
	private Object obj;
	private List<Class<?>> interfs;
	private List<MethodHandler> handlers = Lists.newArrayList();

	public ProviderInstance(String namespace, Object obj, List<Class<?>> interfs) {
		this.namespace = namespace;
		this.obj = obj;
		this.interfs = interfs;
	}

	public String getNamespace() {
		return namespace;
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
