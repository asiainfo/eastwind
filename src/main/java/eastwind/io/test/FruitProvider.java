package eastwind.io.test;

import java.util.List;

public interface FruitProvider {

	public List<Fruit> queryAll();

	public int create(String name);

}
