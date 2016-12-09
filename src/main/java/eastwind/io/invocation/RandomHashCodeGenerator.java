package eastwind.io.invocation;

public class RandomHashCodeGenerator implements HashCodeGenerator {

	public static final RandomHashCodeGenerator DEFAULT = new RandomHashCodeGenerator();
	
	@Override
	public int hashCode(InvocationInfo info) {
		return (int) System.nanoTime();
	}

}