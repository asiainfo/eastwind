package eastwind.io.common;


public abstract class InvocationListener<R> {

	protected Object attach;

	public abstract void operationComplete(R result, Throwable th);

	public void setAttach(Object attach) {
		this.attach = attach;
	}
}
