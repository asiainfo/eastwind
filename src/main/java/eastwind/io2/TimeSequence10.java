package eastwind.io2;

public class TimeSequence10 {

	private long lastId;

	public TimeSequence10() {
		this.lastId = System.currentTimeMillis() * 10;
	}

	public synchronized long newId() {
		long now = System.currentTimeMillis() * 10;
		if (now <= lastId) {
			now = ++lastId;
		} else {
			lastId = now;
		}
		return now;
	}
}