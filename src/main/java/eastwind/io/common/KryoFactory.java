package eastwind.io.common;

import com.esotericsoftware.kryo.Kryo;

public class KryoFactory {

	private static ThreadLocal<Kryo> KRYO_LOCAL = new ThreadLocal<Kryo>();

	public static Kryo getLocalKryo() {
		Kryo kryo = KRYO_LOCAL.get();
		if (kryo == null) {
			kryo = getKryo();
			KRYO_LOCAL.set(kryo);
		}
		return kryo;
	}

	public static Kryo getKryo() {
		Kryo kryo = new Kryo();
		kryo.register(Request.class, 11);
		kryo.register(Respone.class, 12);
		kryo.register(Messaging.class, 13);
		kryo.register(Host.class, 14);
		kryo.register(Handshake.class, 15);

		kryo.register(Ping.class, 126);
		kryo.register(ShutdownObj.class, 127);
		return kryo;
	}

}
