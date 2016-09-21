package eastwind.io3.transport;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;
import eastwind.io3.Sequence;
import eastwind.io3.TransmitSustainer;
import eastwind.io3.obj.Host;
import eastwind.io3.obj.Shake;

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

	public ServerTransport serverTransport(String group, Host host) {
		ChannelFuture cf = bootstrap.connect(host.getIp(), host.getPort());
		ServerTransport st = new ServerTransport(group, host, cf.channel());
		cf.addListener(new ConnectListener(st));
		return st;
	}

	private class ConnectListener implements GenericFutureListener<ChannelFuture> {

		private ServerTransport st;

		public ConnectListener(ServerTransport st) {
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
