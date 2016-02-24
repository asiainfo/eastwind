package eastwind.io2.server;

import io.netty.channel.Channel;

import java.util.Map;

import com.google.common.collect.Maps;

import eastwind.io2.Request;
import eastwind.io2.Response;

public class InboundChannel {

	private String app;
	private String uuid;
	private Channel channel;
	private Map<Long, Request> requests = Maps.newHashMap();
	private Map<Long, Response> responses = Maps.newHashMap();

	public InboundChannel(String app, String uuid) {
		this.app = app;
		this.uuid = uuid;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public String getUuid() {
		return uuid;
	}

	public String getApp() {
		return app;
	}

	public void addRequest(Request request) {
		synchronized (requests) {
			this.requests.put(request.getHeader().getId(), request);
		}
	}
	
	public void removeRequest(Long id) {
		synchronized (requests) {
			this.requests.remove(id);
		}
	}
	
	public void addResponse(Response response) {
		synchronized (responses) {
			this.responses.put(response.getHeader().getId(), response);
		}
	}
	
	public void removeResponse(Long id) {
		synchronized (responses) {
			this.responses.remove(id);
		}
	}
}
