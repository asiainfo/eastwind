package eastwind.io2;

import java.io.ByteArrayOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class Request {

	private RequestHeader requestHeader;
	private Object arg;

	public RequestHeader getHeader() {
		return requestHeader;
	}

	public void setHeader(RequestHeader requestHeader) {
		this.requestHeader = requestHeader;
	}

	public Object getArg() {
		return arg;
	}

	public void setArg(Object arg) {
		this.arg = arg;
	}
	
	public static void main(String[] args) {
		Kryo kryo = new Kryo();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Output output = new Output(baos);
		kryo.writeClassAndObject(output, new int[] { 999 });
		kryo.writeClassAndObject(output, 123);
		output.flush();

		System.out.println(baos.size());

		Input input = new Input(baos.toByteArray());
		// System.out.println(((int[]) kryo.readClassAndObject(input))[0]);
		// System.out.println(kryo.readClassAndObject(input));
	}
}
