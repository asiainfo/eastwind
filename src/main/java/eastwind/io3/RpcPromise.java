package eastwind.io3;

import com.google.common.util.concurrent.AbstractFuture;

public class RpcPromise<T> extends AbstractFuture<T> {

	protected Request request;
	protected Response response;

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public Response getResponse() {
		return response;
	}

	@SuppressWarnings("unchecked")
	public void succeeded(Response response) {
		this.response = response;
		super.set((T) response.getResult());
	}
}
