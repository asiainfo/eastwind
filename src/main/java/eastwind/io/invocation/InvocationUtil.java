package eastwind.io.invocation;

public class InvocationUtil {

	public static void makeNextInvocationAsync() {
		InvocationMade made = new InvocationMade();
		made.setSync(false);
		InvocationMade.TL.set(made);
	}
	
	public static void makeNextInvocation() {
		InvocationMade made = new InvocationMade();
		made.setSync(InvocationMade.DEFAULT.isSync());
		made.setRetry(InvocationMade.DEFAULT.getRetry());
		made.setTimeout(InvocationMade.DEFAULT.getTimeout());
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