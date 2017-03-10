package eastwind.io2;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import eastwind.io.support.GlobalExecutor;

public class Exchange implements Future<Object> {

	private Request request;
	private Response response;
	private SettableFuture<Object> future = new SettableFuture<Object>();

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public Response getResponse() {
		return response;
	}

	public boolean setResponse(Response response) {
		boolean seted = false;
		if (response.getTh() != null) {
			seted = this.future.setException(response.getTh());
		} else {
			seted = this.future.set(response.getResult());
		}
		if (seted) {
			this.response = response;
		}
		return seted;
	}

	public void addListener(final ResponseListener listener) {
		ResponseListener2Runnable r = new ResponseListener2Runnable(listener, this);
		this.future.addListener(r, GlobalExecutor.EVENT_EXECUTOR);
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return future.cancel(mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled() {
		return future.isCancelled();
	}

	@Override
	public boolean isDone() {
		return future.isDone();
	}

	@Override
	public Object get() throws InterruptedException, ExecutionException {
		return future.get();
	}

	@Override
	public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return future.get(timeout, unit);
	}

	private static final class ResponseListener2Runnable implements Runnable {
		private final ResponseListener listener;
		private final Exchange exchange;

		public ResponseListener2Runnable(ResponseListener listener, Exchange exchange) {
			this.listener = listener;
			this.exchange = exchange;
		}

		@Override
		public void run() {
			listener.onSuccess(exchange);
		}
	}
}
