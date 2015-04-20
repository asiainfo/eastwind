package eastwind.io.bioclient;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class IoPut {

	private static ThreadLocal<Output> OUT_PUT = new ThreadLocal<Output>() {
		@Override
		protected Output initialValue() {
			return new Output(new FrugalOutputStream());
		}
	};

	private static ThreadLocal<Input> IN_PUT = new ThreadLocal<Input>() {
		@Override
		protected Input initialValue() {
			return new Input(4096);
		}
	};

	private static ThreadLocal<FrugalOutputStream> FRUGAL_OUTPUT_STREAM = new ThreadLocal<FrugalOutputStream>() {
		@Override
		protected FrugalOutputStream initialValue() {
			return new FrugalOutputStream();
		}
	};

	public static Output outPut() {
		return OUT_PUT.get();
	}

	public static Input inPut() {
		return IN_PUT.get();
	}

	public static FrugalOutputStream frugalOutputStream() {
		return FRUGAL_OUTPUT_STREAM.get();
	}
}
