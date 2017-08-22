package eastwind.io3.codex;

import eastwind.io3.support.Ordered;
import eastwind.io3.support.SupportUtil;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class HandlerBarrier extends ChannelInboundHandlerAdapter implements Ordered {

	private String name;
	private HandlerInitializer initializer;

	public HandlerBarrier(HandlerInitializer initializer) {
		this.initializer = initializer;
		this.name = SupportUtil.getName(this) + "#" + SupportUtil.getName(initializer);
	}

	public HandlerInitializer getInitializer() {
		return initializer;
	}

	@Override
	public int getOrder() {
		return initializer.getOrder();
	}
	
	public String getName() {
		return this.name;
	}
}
