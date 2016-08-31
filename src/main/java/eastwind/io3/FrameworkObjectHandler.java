package eastwind.io3;

import io.netty.channel.Channel;
import eastwind.io3.obj.FrameworkObject;
import eastwind.io3.obj.Shake;
import eastwind.io3.obj.UniqueHolder;
import eastwind.io3.transport.ClientRepository;
import eastwind.io3.transport.ClientTransport;
import eastwind.io3.transport.ServerRepository;
import eastwind.io3.transport.ServerTransport;
import eastwind.io3.transport.TransportFactory;

public class FrameworkObjectHandler {

	private Shake myShake;
	private TransportFactory transportFactory;
	private ClientRepository clientRepository;
	private ServerRepository serverRepository;

	public FrameworkObjectHandler(Shake myShake, TransportFactory transportFactory, ClientRepository clientRepository,
			ServerRepository serverRepository) {
		this.myShake = myShake;
		this.transportFactory = transportFactory;
		this.clientRepository = clientRepository;
		this.serverRepository = serverRepository;
	}

	public void handleObjectFromClient(Object obj, Channel channel) {
		String id = channel.id().asShortText();
		ChannelStat cs = ChannelStat.get(channel);
		ClientTransport transport = clientRepository.getTransport(id);
		if (obj instanceof FrameworkObject) {
			if (obj instanceof Shake) {
				Shake shake = (Shake) obj;
				cs.setShaked(true);
				if (transport == null) {
					transport = transportFactory.clientTransport(shake.getGroup(), shake.getUuid(), channel);
					clientRepository.addTransport(transport);
				}
				channel.writeAndFlush(myShake);
			}
		} else if (obj instanceof UniqueHolder) {

		}
	}

	public void handleObjectFromServer(Object obj, Channel channel) {
		String id = channel.id().asShortText();
		ChannelStat cs = ChannelStat.get(channel);
		ServerTransport transport = serverRepository.getTransport(id);
		if (obj instanceof FrameworkObject) {
			if (obj instanceof Shake) {
				Shake shake = (Shake) obj;
				cs.setShaked(true);
				transport.setUuid(shake.getUuid());
				transport.setActualGroup(shake.getGroup());
				transport.setStatus(1);
			}
		} else if (obj instanceof UniqueHolder) {

		}
	}

}
