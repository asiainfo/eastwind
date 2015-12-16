package test2;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.collect.Maps;

import eastwind.io2.Handshake;

public class KryoTest {

	public static void main(String[] args) {
		Map<Object, Object> map = Maps.newHashMap();
		map.put("123", new Date());

		Handshake handshake = new Handshake();
		handshake.setProperties(map);
		
		Kryo kryo = new Kryo();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Output output = new Output(os);
		kryo.writeObject(output, handshake);
		output.flush();
		
		Input input = new Input(os.toByteArray());
		Handshake h = kryo.readObject(input, Handshake.class);
		System.out.println(h.getProperties().get("123"));
	}
	
}
