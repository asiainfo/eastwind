package eastwind.io3;

import io.netty.channel.SimpleChannelInboundHandler;
import eastwind.io3.obj.FrameworkObject;
import eastwind.io3.obj.Shake;
import eastwind.io3.transport.TransportFactory;

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
