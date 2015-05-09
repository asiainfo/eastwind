package eastwind.io.common;

public class TimedIdSequence {

	private long lastId;

	public TimedIdSequence() {
		this.lastId = System.currentTimeMillis();
	}

	public synchronized long newId() {
		long now = System.currentTimeMillis();
		if (now <= lastId) {
			now = ++lastId;
		} else {
			lastId = now;
		}
		return now;
	}
}