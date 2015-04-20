package eastwind.io.test;

public class HelloProviderImpl implements HelloProvider {

	@Override
	public String hello(String name) {
		return "hello " + name;
	}

}
