package eastwind.io.test;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

public class TestClient {

	public static String TEST_CLIENT = "test-client";

	public static void main(String[] args) throws Throwable {
//		EastwindClient client = new EastwindClient(TEST_CLIENT);
//		client.addServer(TestServer.TEST_SERVER, new Host("127.0.0.1", 12468));
//		client.start();
		// Hello hello = client.createInvokerOnBinary(TestServer.TEST_SERVER,
		// Hello.class);
		// System.out.println(hello.hello("eastwind"));

//		SmartInvoker<String> helloInvoker = client.createSmartInvoker(TestServer.TEST_SERVER, "helloImpl.hello",
//				String.class);
//		System.out.println(helloInvoker.invoke("eastwind"));
//		System.in.read();
		
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(new NioEventLoopGroup(2)).channel(NioSocketChannel.class);
		bootstrap.handler(new ChannelInboundHandlerAdapter(){
			@Override
			public void channelActive(ChannelHandlerContext ctx) throws Exception {
				ctx.channel().writeAndFlush(Unpooled.copiedBuffer("GET X", Charset.forName("utf-8")));
			}
		});
		bootstrap.connect("127.0.0.1", 12468);
		System.in.read();
	}

}
