package eastwind.io2;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class SelfDescribedSerializer extends Serializer {

	@Override
	public final Object readByClass(Class<?> cls, InputStream inputStream) {
		return read(inputStream);
	}

	public abstract Object read(InputStream inputStream);
	
	public abstract void write(OutputStream outputStream, Object obj);
	
}
