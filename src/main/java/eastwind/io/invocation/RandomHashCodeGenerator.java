package eastwind.io.invocation;

public class RandomHashCodeGenerator implements HashCodeGenerator {

	@Override
	public int hashCode(InvocationInfo info) {
		return (int) System.nanoTime();
	}

}