package eastwind.io.common;


public abstract class FutureListener<R> {

	protected Object attach;

	public abstract void operationComplete(RequestFuture<R> rf);

	public void setAttach(Object attach) {
		this.attach = attach;
	}
}
