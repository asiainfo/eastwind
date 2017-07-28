package eastwind.io3.connector;

public class MasterChannel extends AbstractChannel {

	protected MasterChannel(io.netty.channel.ChannelFuture future) {
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
