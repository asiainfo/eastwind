package eastwind.io3;

import io.netty.channel.ChannelPromise;
import eastwind.io.common.Host;

public interface ChannelAware extends Application {

	Host getHost();

	ChannelPromise getChannelPromise();
	
	void setChannelPromise(ChannelPromise channelPromise);
	
}
