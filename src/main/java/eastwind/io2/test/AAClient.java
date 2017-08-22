package eastwind.io2.test;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import eastwind.io2.AbstractConfigurablePeer;
import eastwind.io2.ConnectedTransport;
import eastwind.io2.Listener;
import eastwind.io2.NettyConnector;
import eastwind.io2.NettyConnectorFactory;
import eastwind.io2.NettyConnectorFactorys;
import eastwind.io2.Request;
import eastwind.io2.Shake;
import eastwind.io2.Transport;

public class AAClient extends AbstractConfigurablePeer {

    protected AAClient(String group) {
        super.uuid = UUID.randomUUID().toString();
        super.group = group;
    }

    @Override
    protected NettyConnectorFactory getConnectorFactory() {
        return NettyConnectorFactorys.clientConnectorFactory();
    }

    public void start() {
        super.start(new InetSocketAddress(12469));
    }

    public static final String TEST_CLIENT = "TEST_CLIENT";

    public static void main(String[] args) throws IOException, InterruptedException {
        AAClient client = new AAClient(TEST_CLIENT);
        client.start();

        final Request request = new Request();
        request.setDataLength(2);
        request.setData(new String[]{"abcaaaaaaaaaaaaaaaaaaaaaafffffffffffffffffffffffffffffffeeeeeeeeeeeeeeeeeeeeeessssssssssssssssssssssssssssssssss", "123aaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbcccccccccccccccccccccccccccccccccccccccccccdddddddddddddddddddddddddddddddddddddddddddeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaafffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffgggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggfffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkknnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"});

        NettyConnectorFactory fac =
                NettyConnectorFactorys.clientConnectorFactory();
        NettyConnector connector = fac.createConnector(1, 2);
        final ConnectedTransport transport =
                connector.connect(AAServer.TEST_SERVER, new
                        InetSocketAddress("127.0.0.1", 12469));
//		 transport.addActiveListener(new Listener<Transport>() {
//		 @Override
//		 public void listen(Transport t) {
//		 transport.post(new Shake());
//		 transport.post(request);
//		 }
//		 });

        ChannelFuture cf = transport.getFuture();
        cf.await();
        System.out.println(cf.isSuccess());
        Channel c = cf.channel();
        c.writeAndFlush(new Shake());
        for (; ; ) {
            c.writeAndFlush(request);
            c.writeAndFlush(request);
            TimeUnit.SECONDS.sleep(10000000);
        }

//		client.refresh(AAServer.TEST_SERVER, Sets.newHashSet((SocketAddress) new InetSocketAddress(12469)));
    }

}
