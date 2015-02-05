package boc.message;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class IoPut {

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

	public static Output outPut() {
		return OUT_PUT.get();
	}

	public static Input inPut() {
		return IN_PUT.get();
	}
}
