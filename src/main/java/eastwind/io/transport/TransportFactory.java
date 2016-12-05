package eastwind.io.transport;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;
import eastwind.io.Sequence;
import eastwind.io.TransmitSustainer;
import eastwind.io.model.Host;
import eastwind.io.model.Shake;

public class TransportFactory {

	private Bootstrap bootstrap;
	private Shake shake;
	private Sequence sequence;
	private TransmitSustainer transmitSustainer;

	public TransportFactory(Bootstrap bootstrap, Shake shake, Sequence sequence, TransmitSustainer transmitSustainer) {
		this.bootstrap = bootstrap;
		this.shake = shake;
		this.sequence = sequence;
		this.transmitSustainer = transmitSustainer;
	}

	public ClientTransport clientTransport(String group, String uuid, Channel channel) {
		return new ClientTransport(group, uuid, channel);
	}

	public ServerTransport serverTransport(String group, Node node) {
		Host host = node.getHost();
		ChannelFuture cf = bootstrap.connect(host.getIp(), host.getPort());
		ServerTransport st = new ServerTransport(group, node, cf.channel());
		cf.addListener(new Connect2ShakeListener(st));
		return st;
	}

	private class Connect2ShakeListener implements GenericFutureListener<ChannelFuture> {

		private ServerTransport st;

		public Connect2ShakeListener(ServerTransport st) {
			this.st = st;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (future.isSuccess()) {
				st.setSequence(sequence);
				st.setTransmitSustainer(transmitSustainer);
				st.send(shake);
			} else {

			}
		}

	}
}
