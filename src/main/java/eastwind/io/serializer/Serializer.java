package eastwind.io.serializer;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

public interface Serializer {

	public Object read(Type type, InputStream inputStream);

	public void write(Object obj, OutputStream outputStream);

}
