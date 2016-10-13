package eastwind.io.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSerializer implements Serializer {

	protected ObjectMapper objectMapper;

	public JsonSerializer() {
		objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		objectMapper.enable(Feature.ALLOW_UNQUOTED_FIELD_NAMES);
		objectMapper.enable(Feature.ALLOW_SINGLE_QUOTES);
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	@Override
	public Object read(Type type, InputStream inputStream) {
		try {
			return objectMapper.readValue(inputStream, objectMapper.getTypeFactory().constructType(type));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void write(Object obj, OutputStream outputStream) {
		try {
			objectMapper.writeValue(outputStream, obj);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
