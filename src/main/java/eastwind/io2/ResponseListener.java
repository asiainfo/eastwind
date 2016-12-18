package eastwind.io2;

import java.util.EventListener;

public interface ResponseListener extends EventListener {

	void onSuccess(Exchange exchange);

	void onInvokeException(Exchange exchange);

	void onExecutionException(Exchange exchange);

	void onCanceled(Exchange exchange);

}
