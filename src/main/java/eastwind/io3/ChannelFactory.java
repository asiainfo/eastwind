package eastwind.io3;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import eastwind.io.common.Host;

public class ChannelFactory {

	private Bootstrap bootstrap;

	public ChannelFactory(Bootstrap bootstrap) {
		this.bootstrap = bootstrap;
	}

	public void newChannel(ChannelAware channelAware) {
		synchronized (channelAware) {
			ChannelPromise channelPromise = channelAware.getChannelPromise();
			if (channelPromise != null) {
				if (channelPromise.isSuccess() && channelPromise.channel().isActive()) {
					return;
				}
				if (!channelPromise.isDone() && channelPromise.channel().isOpen()) {
					return;
				}
			}

			Host host = channelAware.getHost();
			ChannelFuture cf = bootstrap.connect(host.getIp(), host.getPort());
			
		}
	}
}
