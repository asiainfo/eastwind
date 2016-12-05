package eastwind.io.invocation;

public class PropertyHashCodeGenerator implements HashCodeGenerator {

	private String expression;
	
	public PropertyHashCodeGenerator(String expression) {
		this.expression = expression;
	}

	@Override
	public int hashCode(InvocationInfo info) {
		return 0;
	}
	
}
