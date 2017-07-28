package eastwind.io3.connector;

public class OutputChannel extends AbstractChannel {

	protected OutputChannel(io.netty.channel.ChannelFuture future) {
		super(future.channel());
		future.addListener(cf -> {
			if (cf.isSuccess()) {
				active();
			} else {
				activeFailed(cf.cause());
			}
		});
	}
	
}
