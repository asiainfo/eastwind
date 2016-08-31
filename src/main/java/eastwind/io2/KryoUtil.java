package eastwind.io2;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import eastwind.io3.obj.Header;

public class KryoUtil {

	private static ThreadLocal<Kryo> KRYO_LOCAL = new ThreadLocal<Kryo>() {
		@Override
		protected Kryo initialValue() {
			return createKryo();
		}
	};

	private static ThreadLocal<Output> OUT_PUT = new ThreadLocal<Output>() {
		@Override
		protected Output initialValue() {
			return new Output(4096);
		}
	};

	private static ThreadLocal<Input> IN_PUT = new ThreadLocal<Input>() {
		@Override
		protected Input initialValue() {
			return new Input(4096);
		}
	};

	public static Output getOutPut() {
		return OUT_PUT.get();
	}

	public static Input getInPut() {
		return IN_PUT.get();
	}
	
	public static Kryo getKryo() {
		return KRYO_LOCAL.get();
	}

	public static Kryo createKryo() {
		Kryo kryo = new Kryo();
		kryo.register(Header.class, 11);
		return kryo;
	}

}
