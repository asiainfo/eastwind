package eastwind.io;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import eastwind.io.model.FrameworkObject;
import eastwind.io.model.Shake;
import eastwind.io.model.UniqueHolder;
import eastwind.io.transport.ServerRepository;
import eastwind.io.transport.ServerTransport;
import eastwind.io.transport.TransportFactory;

@Sharable
public class ClientFrameworkHandler extends FrameworkHandler {

	private ServerRepository serverRepository;

	public ClientFrameworkHandler(Shake myShake, TransmitSustainer transmitSustainer,
			TransportFactory transportFactory, ServerRepository serverRepository) {
		super(myShake, transmitSustainer, transportFactory);
		this.serverRepository = serverRepository;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FrameworkObject obj) throws Exception {
		Channel channel = ctx.channel();
		String id = channel.id().asShortText();
		ChannelStat stat = ChannelStat.get(channel);
		ServerTransport transport = serverRepository.getTransport(id);
		if (obj instanceof UniqueHolder) {
			UniqueHolder holder = (UniqueHolder) obj;
			if (holder.getPreId() != 0) {
				TransmitPromise promise = transmitSustainer.remove(holder.getPreId());
				if (promise != null) {
					promise.set(holder.getObj());
				}
			}
		} else {
			if (obj instanceof Shake) {
				Shake shake = (Shake) obj;
				stat.setShaked(true);
				transport.setUuid(shake.getUuid());
				transport.setActualGroup(shake.getGroup());
				transport.setStatus(1);
			}
		}
	}

}
