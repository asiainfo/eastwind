package eastwind.io.nioclient;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentMap;

import eastwind.io.ChannelAttr;
import eastwind.io.common.Host;
import eastwind.io.common.InterfAb;
import eastwind.io.common.Request;

public class ClientOutboundHandler extends ChannelOutboundHandlerAdapter {

	private ConcurrentMap<Host, InterfAb> interfAbs;

	public ClientOutboundHandler(ConcurrentMap<Host, InterfAb> interfAbs) {
		this.interfAbs = interfAbs;
	}

	@Override
	public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
			ChannelPromise promise) throws Exception {
		Host host = ChannelAttr.get(ctx.channel(), ChannelAttr.REMOTE_HOST);
		ChannelAttr.set(ctx.channel(), ChannelAttr.INTERF_AB, interfAbs.get(host));
		super.connect(ctx, remoteAddress, localAddress, promise);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		if (msg instanceof Request) {
			Request request = (Request) msg;
			InterfAb interfAb = ChannelAttr.get(ctx.channel(), ChannelAttr.INTERF_AB);
			String interfId = interfAb.getInterfId(request.getInterf());
			if (interfId != null) {
				request.setInterf(interfId);
			}
		}
		super.write(ctx, msg, promise);
	}

}
