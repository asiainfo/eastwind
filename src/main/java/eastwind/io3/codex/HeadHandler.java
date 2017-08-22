package eastwind.io3.codex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import eastwind.io3.connector.InputChannelHandler;
import eastwind.io3.support.SupportUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;

public class HeadHandler extends ChannelInboundHandlerAdapter {

	private CodexHolder codexHolder = new CodexHolder();
	private HandlerInitializerSelector initializerSelector;
	private List<HandlerBarrier> headBarriers = new ArrayList<>();

	public HeadHandler(HandlerInitializerSelector initializerSelector) {
		this.initializerSelector = initializerSelector;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf in = (ByteBuf) msg;
		// CodexHolder
		HandlerInitializer initializer = null;
		if (codexHolder.isHolded()) {
			initializer = codexHolder.getHolder();
		} else {
			initializer = initializerSelector.select(in);
		}

		if (initializer == null) {
			return;
		}
		HandlerBarrier thisBarrier = findHeadBarrier(initializer);
		if (thisBarrier == null) {
			thisBarrier = new HandlerBarrier(initializer);
			SupportUtil.add(headBarriers, thisBarrier);
			HandlerBarrier beforeOfBarrier = findBeforeOfBarrier(ctx, initializer);

			ChannelPipeline pipeline = ctx.pipeline();
			if (beforeOfBarrier == null) {
				pipeline.addLast(thisBarrier.getName(), thisBarrier);
			} else {
				pipeline.addBefore(beforeOfBarrier.getName(), thisBarrier.getName(), thisBarrier);
			}
			List<ChannelHandler> newhandlers = initializer.handlers();
			ListIterator<ChannelHandler> it = newhandlers.listIterator(newhandlers.size());
			while (it.hasPrevious()) {
				ChannelHandler ch = it.previous();
				if (ch instanceof CodexHolderAware) {
					CodexHolderAware cha = (CodexHolderAware) ch;
					cha.setCodexHolder(codexHolder);
				}
				pipeline.addAfter(thisBarrier.getName(), SupportUtil.getName(ch), ch);
			}
		}
		codexHolder.setHolder(thisBarrier.getInitializer());
		ctx.pipeline().context(thisBarrier).fireChannelRead(msg);
	}

	private HandlerBarrier findBeforeOfBarrier(ChannelHandlerContext ctx, HandlerInitializer initializer) {
		HandlerBarrier beforeOfBarrier = null;
		Map<String, ChannelHandler> handlers = ctx.pipeline().toMap();
		for (;;) {
			HandlerInitializer before = initializerSelector.before(initializer);
			if (before == null) {
				break;
			}
			Iterator<Entry<String, ChannelHandler>> it = handlers.entrySet().iterator();
			boolean finded = false;
			while (it.hasNext()) {
				Entry<String, ChannelHandler> entry = it.next();
				ChannelHandler ch = entry.getValue();
				if (ch instanceof HandlerBarrier) {
					HandlerBarrier hb = (HandlerBarrier) ch;
					if (hb.getInitializer() == before) {
						finded = true;
						break;
					}
				}
			}
			while (it.hasNext()) {
				Entry<String, ChannelHandler> entry = it.next();
				ChannelHandler ch = entry.getValue();
				if (ch instanceof HandlerBarrier) {
					beforeOfBarrier = (HandlerBarrier) ch;
				}
			}
			if (finded) {
				break;
			}
		}
		return beforeOfBarrier;
	}

	private HandlerBarrier findHeadBarrier(HandlerInitializer initializer) {
		for (HandlerBarrier headBarrier : headBarriers) {
			if (headBarrier.getInitializer() == initializer) {
				return headBarrier;
			}
		}
		return null;
	}
}
