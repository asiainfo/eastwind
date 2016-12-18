package eastwind.io2;

public interface Result<T> {

	boolean isCanceled();
	
	boolean isSuccess();
	
	boolean isFailed();
	
	T getValue();
	
	Throwable getTh();
}
