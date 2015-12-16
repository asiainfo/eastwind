package eastwind.io2.client;

public class VoidListener extends ResultListener<Object> {

	@Override
	protected final void onSuccess(Object result) {
		onSuccess();
	}

	protected void onSuccess() {

	}

}
