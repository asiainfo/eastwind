package eastwind.io3;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import eastwind.io3.model.FrameworkObject;
import eastwind.io3.model.HandlerMetaData;
import eastwind.io3.model.MethodEnquire;
import eastwind.io3.model.Shake;
import eastwind.io3.model.UniqueHolder;
import eastwind.io3.transport.ClientRepository;
import eastwind.io3.transport.ClientTransport;
import eastwind.io3.transport.TransportFactory;

@Sharable
public class ServerFrameworkHandler extends FrameworkHandler {

	private HandlerRegistry handlerRegistry;
	private ClientRepository clientRepository;

	public ServerFrameworkHandler(Shake myShake, TransmitSustainer transmitSustainer, HandlerRegistry handlerRegistry, TransportFactory transportFactory,
			ClientRepository clientRepository) {
		super(myShake, transmitSustainer, transportFactory);
		this.handlerRegistry = handlerRegistry;
		this.clientRepository = clientRepository;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FrameworkObject obj) throws Exception {
		Channel channel = ctx.channel();
		String id = channel.id().asShortText();
		ChannelStat cs = ChannelStat.get(channel);
		ClientTransport transport = clientRepository.getTransport(id);
		if (obj instanceof UniqueHolder) {
			UniqueHolder holder = (UniqueHolder) obj;
			Object content = holder.getObj();
			UniqueHolder reply = null;
			if (content instanceof MethodEnquire) {
				MethodEnquire enquire = (MethodEnquire) content;
				MethodHandler handler = handlerRegistry.findHandler(enquire.getInterf(), enquire.getMethod(), enquire.getParameterTypes());
				HandlerMetaData meta = new HandlerMetaData();
				meta.setName(handler.getAlias());
				reply = UniqueHolder.reply(holder, meta);
			}
			if (reply != null) {
				channel.writeAndFlush(reply);
			}
		} else if (obj instanceof FrameworkObject) {
			if (obj instanceof Shake) {
				Shake shake = (Shake) obj;
				cs.setShaked(true);
				if (transport == null) {
					transport = transportFactory.clientTransport(shake.getGroup(), shake.getUuid(), channel);
					clientRepository.addTransport(transport);
				}
				channel.writeAndFlush(myShake);
			}
		}
	}

}
