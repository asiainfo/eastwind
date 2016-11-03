package eastwind.io.invocation;

public class PropertyCondition {

	private String expression;
	private String symbol;
	private Object value;

	public PropertyCondition(String expression, String symbol, Object value) {
		this.expression = expression;
		this.symbol = symbol;
		this.value = value;
	}

	public String getExpression() {
		return expression;
	}

	public String getSymbol() {
		return symbol;
	}

	public Object getValue() {
		return value;
	}
}
