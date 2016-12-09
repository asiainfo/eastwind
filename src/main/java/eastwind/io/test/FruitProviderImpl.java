package eastwind.io.test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Lists;

import eastwind.io.Provider;

@Provider(name = "fruit")
public class FruitProviderImpl implements FruitProvider {

	private AtomicInteger sequence = new AtomicInteger();
	private List<Fruit> fruits = Lists.newArrayList();
	
	@Override
	public List<Fruit> queryAll() {
		return fruits;
	}

	@Override
	public int create(String name) {
		Fruit fruit = new Fruit();
		fruit.setId(sequence.incrementAndGet());
		fruit.setName(name);
		fruits.add(fruit);
		return fruit.getId();
	}

}
