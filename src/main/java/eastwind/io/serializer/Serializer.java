package eastwind.io.serializer;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

public interface Serializer {

	Object read(Type type, InputStream inputStream);

	void write(Object obj, OutputStream outputStream);

}
