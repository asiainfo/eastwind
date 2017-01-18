package eastwind.io2;

import java.net.InetSocketAddress;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import eastwind.io.ProviderHandler;
import eastwind.io.ProviderRegistry;

public abstract class AbstractConnectorEndPoint extends AbstractEventEndPoint {

	protected Map<String, EndPointGroup> groups = Maps.newHashMap();
	protected EventBus connectorEventBus;

	protected ProviderRegistry internalRegistry = new ProviderRegistry();
	protected ProviderRegistry providerRegistry = new ProviderRegistry();

	protected AbstractConnectorEndPoint(String uuid, String group, String tag, String version) {
		super(uuid, group, tag, version);
	}

	protected void init() {
		connectorEventBus = createEventBus(tag, null);
	}
	
	@Override
	protected ListenerFactory getListenerFactory(NetworkTrafficTransport transport) {
		return new OutboundListenerFactory((OutboundTransport) transport);
	}

	protected OutboundTransport connect(String group, InetSocketAddress remoteAddress) {
		OutboundTransport transport = nettyConnector.connect(group, remoteAddress);
		listenTo(transport);
		return transport;
	}

	class OutboundListenerFactory implements ListenerFactory {

		OutboundTransport transport;

		public OutboundListenerFactory(OutboundTransport transport) {
			this.transport = transport;
		}

		@Override
		public Listener<Transport> getActiveListener() {
			return new Listener<Transport>() {
				@Override
				public void listen(Transport t) {
					if (transport.isActived()) {
						Shake shake = createShake();
						transport.post(shake);
					}
				}
			};
		}

		@Override
		public Listener<Shake> getShakeListener() {
			return new Listener<Shake>() {
				@Override
				public void listen(Shake t) {
					RemoteEndPoint rep = getOrCreateEndPoint(t.getMyGroup(), t.getMyUuid());
					rep.attach(transport);
					transport.push(t);
				}
			};
		}

		@Override
		public Listener<NetworkTraffic> getNetworkTrafficListener() {
			return new Listener<NetworkTraffic>() {
				@Override
				public void listen(NetworkTraffic t) {
					connectorEventBus.post(t);
				}
			};
		}
	}

	protected class NetworkTrafficEventListener {
		@Subscribe
		public void listen(Request request) throws Exception {
			ProviderRegistry registry = request.isInternal() ? internalRegistry : providerRegistry;
			ProviderHandler ph = registry.findHandler(request.getName());
			Object[] params = null;
			if (request.getDataLength() == 1) {
				params = new Object[] { request.getData() };
			} else if (request.getDataLength() > 1) {
				params = (Object[]) request.getData();
			}
			Object result = ph.invoke(params);
			if (result != Void.INSTANCE && !(result instanceof Void)) {
				Response response = new Response();
				response.setId(request.getId());
				response.setSerializer(request.getSerializer());
				response.setData(result);
				request.getTransport().post(response);
			}
		}

		@Subscribe
		public void listen(Response response) {
			System.out.println(JSON.toJSONString(response));
		}
	}

	protected EndPointGroup getEndPointGroup(String group) {
		EndPointGroup g = groups.get(group);
		if (g == null) {
			g = new EndPointGroup(group);
		}
		return g;
	}

	protected RemoteEndPoint getOrCreateEndPoint(String group, String uuid) {
		EndPointGroup g = getEndPointGroup(group);
		RemoteEndPoint endPoint = g.getEndPoint(uuid);
		if (endPoint == null) {
			return g.createEndPoint(uuid);
		}
		return endPoint;
	}

}
