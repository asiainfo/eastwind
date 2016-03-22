package eastwind.io3;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;

import eastwind.io.common.Host;

public class ApplicationManager implements ApplicatioinActivator {

	private Application application;

	private Map<String, TransportableApplicationGroup> groups = Maps.newHashMap();

	private ConcurrentMap<Host, TransportableApplication> applications = Maps.newConcurrentMap();

	private Bootstrap bootstrap;

	public ApplicationManager(Application application, Bootstrap bootstrap) {
		this.application = application;
		this.bootstrap = bootstrap;
	}

	public TransportableApplicationGroup getOrCreate(String group) {
		TransportableApplicationGroup ag = groups.get(group);
		if (ag != null) {
			return ag;
		}
		synchronized (groups) {
			ag = groups.get(group);
			if (ag == null) {
				ag = new TransportableApplicationGroup(group);
				groups.put(group, ag);
			}
		}
		return ag;
	}

	public TransportableApplicationGroup setApplicationConfig(String group, List<Host> hosts) {
		TransportableApplicationGroup g = new TransportableApplicationGroup(group, hosts, this);
		groups.put(group, g);
		return g;
	}

	@Override
	public void active(TransportableApplication app) {
		Host host = app.getHost();
		if (applications.putIfAbsent(host, app) != app) {
			applications.put(host, app);
		}
		final Transport transport = new Transport();
		app.setOutboundTransport(transport);
		
		final ChannelFuture cf = bootstrap.connect(host.getIp(), host.getPort());
		transport.setChannel(cf.channel());
		cf.channel().attr(ChannelAttr.APPLICATION).set(app);
		final String uuid = app.getUuid();
		cf.addListener(new GenericFutureListener<ChannelFuture>() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					Handshake hs = new Handshake();
					hs.setGroup(application.getGroup());
					hs.setMyUuid(application.getUuid());
					hs.setYourUuid(uuid);
					future.channel().writeAndFlush(hs);
				} else {
					transport.getTransportPromise().failed(future.cause());
				}
			}
		});
	}
}
