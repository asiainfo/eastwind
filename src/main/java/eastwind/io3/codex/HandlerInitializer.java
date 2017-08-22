package eastwind.io3.codex;

import eastwind.io3.support.Ordered;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;

import java.util.List;

public abstract class HandlerInitializer<I> implements Ordered {

	protected Object context;

	public enum MatchState {
		MATCHED, UN_MATCHED, MATCHING;
	}

	public abstract MatchState match(ByteBuf in);

	public abstract List<ChannelHandler> handlers();

	public Object getContext() {
		return context;
	}

	public void setContext(Object context) {
		this.context = context;
	}

	public int getOrder() {
		return 0;
	}

}
