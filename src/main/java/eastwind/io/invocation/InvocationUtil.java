package eastwind.io.invocation;

public class InvocationUtil {

	private static final InvocationUtil INSTANCE = new InvocationUtil();
	
	public static InvocationUtil makeNextAsync() {
		InvocationMade made = new InvocationMade();
		made.setSync(false);
		InvocationMade.TL.set(made);
		return INSTANCE;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> InvocationFuture<T> getLast(T returnStub) {
		return InvocationPromise.TL.get();
	}
	
	@SuppressWarnings("unchecked")
	public static InvocationFuture<Object> getLast() {
		return InvocationPromise.TL.get();
	}
	
	public <T> InvocationFuture<T> get(T returnStub) {
		return getLast(returnStub);
	}
}