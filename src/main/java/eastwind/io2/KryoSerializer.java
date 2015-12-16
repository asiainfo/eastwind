package eastwind.io2;

import java.io.InputStream;
import java.io.OutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;


public class KryoSerializer extends SelfDescribedSerializer {

	private Kryo kryo = new Kryo();
	
	@Override
	public Object read(InputStream inputStream) {
		Input input = new Input(inputStream);
		return kryo.readClassAndObject(input);
	}

	@Override
	public void write(OutputStream outputStream, Object obj) {
		Output output = new Output(outputStream);
		kryo.writeClassAndObject(output, obj);
		output.flush();
	}

}
