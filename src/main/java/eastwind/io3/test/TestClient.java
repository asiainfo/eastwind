package eastwind.io3.test;

import java.io.IOException;
import java.lang.reflect.Method;

import eastwind.io3.EastwindFramework;
import eastwind.io3.obj.Host;
import eastwind.io3.transport.ServerTransport;
import eastwind.io3.transport.TransportFactory;

public class TestClient {

	public static String TEST_CLIENT = "test-client";
	
	public static void main(String[] args) throws IOException, NoSuchMethodException, SecurityException {
		EastwindFramework eastwind = new EastwindFramework(TEST_CLIENT, false);
		eastwind.start();
		TransportFactory transportFactory = eastwind.getTransportFactory();
		Host host = new Host("127.0.0.1", 12468);
		ServerTransport st = transportFactory.serverTransport(TestServer.TEST_SERVER, host);
		Method method = Hello.class.getMethod("hello", String.class);
		st.getHandlerMetaData(method);
		System.in.read();
	}
	
}
