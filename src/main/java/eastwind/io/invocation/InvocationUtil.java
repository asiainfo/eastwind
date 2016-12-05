package eastwind.io.invocation;

public class InvocationUtil {

	public static void makeNextInvocationAsync() {
		InvocationMade made = new InvocationMade();
		made.setSync(false);
		InvocationMade.TL.set(made);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> InvocationFuture<T> getLastInvocation(T returnStub) {
		return InvocationPromise.TL.get();
	}
	
	@SuppressWarnings("unchecked")
	public static InvocationFuture<Object> getLastInvocation() {
		return InvocationPromise.TL.get();
	}
}