package eastwind.io3;

import eastwind.io3.obj.Unique;
import eastwind.io3.support.SettableFuture;

public class TransmitPromise<V> extends SettableFuture<V> implements Unique {

	private int status;
	private Unique message;
	
	public TransmitPromise(Unique message) {
		this.message = message;
	}

	@Override
	public long getId() {
		return message.getId();
	}

	@Override
	public void setId(long id) {
		message.setId(id);
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Unique getMessage() {
		return message;
	}
}
