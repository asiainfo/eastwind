package eastwind.io.server;

import io.netty.channel.Channel;

import java.util.List;

import eastwind.io.common.Request;

public class FilterChain {

	private ProviderHandler handler;
	private Channel channel;
	private List<Filter> filters;
	private Request request;
	private Object result;
	private Throwable th;

	private int pos;

	public FilterChain(ProviderHandler handler, Channel channel, List<Filter> filters, Request request) {
		this.handler = handler;
		this.channel = channel;
		this.filters = filters;
		this.request = request;
	}

	public void doNextFilter() {
		if (pos == filters.size()) {
			Object result = null;
			Throwable th = null;
			try {
				result = handler.invoke(request.getArgs());
			} catch (Throwable e) {
				th = e;
			} finally {
				this.result = result;
				this.th = th;
			}
		} else {
			filters.get(pos++).doFilter(this);
		}
	}

	public Channel getChannel() {
		return channel;
	}

	public Request getRequest() {
		return request;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public Throwable getTh() {
		return th;
	}

	public void setTh(Throwable th) {
		this.th = th;
	}

}
