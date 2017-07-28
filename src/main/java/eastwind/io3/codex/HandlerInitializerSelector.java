package eastwind.io3.codex;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import eastwind.io3.support.SupportUtil;

public class HandlerInitializerSelector {

	private List<HandlerInitializer> initializers = new ArrayList<>();

	public HandlerInitializer before(HandlerInitializer initializer) {
		int i = 0;
		for (; i < initializers.size(); i++) {
			if (initializers.get(i) == initializer) {
				break;
			}
		}
		return i == 0 ? null : initializers.get(i - 1);
	}
	
	public HandlerInitializer select(ByteBuf buf) throws NoMatchedInitializerException {
		boolean matching = false;
		for (HandlerInitializer initializer : initializers) {
			HandlerInitializer.MatchState matchState = initializer.match(buf);
			if (matchState == HandlerInitializer.MatchState.MATCHED) {
				return  initializer;
			}
			if (matchState == HandlerInitializer.MatchState.MATCHING) {
				matching = true;
				break;
			}
		}
		if (matching) {
			return null;
		}
		throw new NoMatchedInitializerException();
	}

	// small order is before
	public void add(HandlerInitializer initializer) {
		SupportUtil.add(initializers, initializer);
	}
}
