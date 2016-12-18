package eastwind.io2;

public class DefaultResult<T> implements Result<T> {

	private static final byte SUCCESS = 1;
	private static final byte FAILED = 2;
	private static final byte CANCELED = 3;
	
	private byte state;
	private T value;
	private Throwable th;
	
	@Override
	public boolean isCanceled() {
		return state == CANCELED;
	}

	@Override
	public boolean isSuccess() {
		return state == SUCCESS;
	}

	@Override
	public boolean isFailed() {
		return state == FAILED;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public Throwable getTh() {
		return th;
	}

}
