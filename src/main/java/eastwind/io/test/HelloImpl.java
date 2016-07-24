package eastwind.io.test;

import java.util.concurrent.TimeUnit;

public class HelloImpl implements Hello {

	@Override
	public String hello(String name) {
		try {
			TimeUnit.SECONDS.sleep(3);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "hello " + name;
	}

}
