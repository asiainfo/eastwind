package eastwind.io3;

import io.netty.channel.Channel;

import java.util.Map;

import com.google.common.collect.Maps;

import eastwind.io.common.Host;

public class TransportableApplication extends GenericApplication {

	protected Host host;
	protected Transport inboundTransport;
	protected Transport outboundTransport;

	@SuppressWarnings("rawtypes")
	protected Map<Long, RpcPromise> rpcPromises = Maps.newConcurrentMap();

	
	
	protected TransportableApplicationGroup transportableApplicationGroup;

	TransportableApplication(String group) {
		super(group);
	}

	TransportableApplication(String group, Host host) {
		super(group);
		this.host = host;
	}

	Transport getInboundTransport() {
		return inboundTransport;
	}

	void setInboundTransport(Transport inboundTransport) {
		this.inboundTransport = inboundTransport;
	}

	Transport getOutboundTransport() {
		return outboundTransport;
	}

	void setOutboundTransport(Transport outboundTransport) {
		this.outboundTransport = outboundTransport;
	}

	void setUuid(String uuid) {
		super.uuid = uuid;
	}

	Transport getTransport(Channel channel) {
		if (outboundTransport != null && channel == outboundTransport.getChannel()) {
			return outboundTransport;
		}
		if (inboundTransport != null && channel == inboundTransport.getChannel()) {
			return inboundTransport;
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public RpcPromise remove(Long id) {
		return rpcPromises.remove(id);
	}
	
	public <T> RpcPromise<T> rpc(Request request) {
		RpcPromise<T> rpcPromise = new RpcPromise<T>();
		rpcPromise.setRequest(request);
		rpcPromises.put(request.getId(), rpcPromise);
		Channel channel = outboundTransport.getChannel();
		channel.writeAndFlush(rpcPromise);
		return rpcPromise;
	}

	public void send(Object object) {
		outboundTransport.send(object);
	}

	public Host getHost() {
		return host;
	}

}
