package eastwind.io3.connector;

import eastwind.io3.codex.HandlerInitializerSelector;
import eastwind.io3.codex.HeadHandler;
import io.netty.channel.ChannelHandlerContext;

public class InputHeadHandler extends HeadHandler {

	private InputChannelHandler inputChannelHandler;

	public InputHeadHandler(HandlerInitializerSelector initializerSelector, InputChannelHandler inputChannelHandler) {
		super(initializerSelector);
		this.inputChannelHandler = inputChannelHandler;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		InputChannel inputChannel = new InputChannel();
		inputChannelHandler.handle(inputChannel);
	}
	
}
