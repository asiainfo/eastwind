package eastwind.io2;

import java.io.InputStream;

public abstract class Serializer {

	public abstract Object readByClass(Class<?> cls, InputStream inputStream);

}
