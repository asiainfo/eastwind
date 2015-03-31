package boc.message.common;

import com.esotericsoftware.kryo.Kryo;

public class KryoFactory {

	private static Kryo kryo = new Kryo();

	static {
		kryo.register(Request.class, 11);
		kryo.register(Respone.class, 12);
		kryo.register(Inform.class, 13);
		kryo.register(Host.class, 14);
		kryo.register(Handshake.class, 15);
		
		kryo.register(Ping.class, 126);
		kryo.register(ShutdownObj.class, 127);
	}

	public static Kryo getKryo() {
		return kryo;
	}

}
