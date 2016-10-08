package eastwind.io3.serializer;

import java.io.InputStream;
import java.io.OutputStream;

public interface Serializer {

	public <T> T read(Class<T> cls, InputStream inputStream);

	public void write(Object obj, OutputStream outputStream);
}
