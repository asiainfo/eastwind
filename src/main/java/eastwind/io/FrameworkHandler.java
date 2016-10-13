package eastwind.io;

import io.netty.channel.SimpleChannelInboundHandler;
import eastwind.io.model.FrameworkObject;
import eastwind.io.model.Shake;
import eastwind.io.transport.TransportFactory;

public abstract class FrameworkHandler extends SimpleChannelInboundHandler<FrameworkObject> {

	protected Shake myShake;
	protected TransmitSustainer transmitSustainer;
	protected TransportFactory transportFactory;

	public FrameworkHandler(Shake myShake, TransmitSustainer transmitSustainer, TransportFactory transportFactory) {
		this.myShake = myShake;
		this.transmitSustainer = transmitSustainer;
		this.transportFactory = transportFactory;
	}

}
