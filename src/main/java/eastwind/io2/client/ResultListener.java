package eastwind.io2.client;

public abstract class ResultListener<R> {

	protected Object attach;

	public void operationComplete(R result, Throwable th) {
		if (th == null) {
			onSuccess(result);
		} else {
			onFail(th);
		}
	}

	protected void onSuccess(R result) {

	}

	protected void onFail(Throwable th) {

	}

	public void setAttach(Object attach) {
		this.attach = attach;
	}
}
