package eastwind.io.serializer;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public final class KryoSerializer implements Serializer {

	private Kryo kryo = new Kryo();
	private Input input = new Input(4096);
	private Output output = new Output(4096);
	
	public Kryo getKryo() {
		return kryo;
	}

	@Override
	public Object read(Type type, InputStream inputStream) {
		input.setInputStream(inputStream);
		return kryo.readClassAndObject(input);
	}

	@Override
	public void write(Object obj, OutputStream outputStream) {
		output.setOutputStream(outputStream);
		kryo.writeClassAndObject(output, obj);
		output.flush();
	}

}
