package eastwind.io2;

public abstract class SyncListener<T> implements Listener<T> {

	private byte[] lock = new byte[1];

	@Override
	public final void listen(T t) {
		synchronized (lock) {
			if (lock[0] == 0) {
				lock[0] = 1;
				try {
					doListen(t);
				} finally {
					lock.notifyAll();
				}
			}
		}
	}

	protected abstract void doListen(T t);

	public void sync() {
		synchronized (lock) {
			if (lock[0] == 0) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}
}
