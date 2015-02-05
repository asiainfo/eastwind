package boc.message.common;

public class TimedIdSequence100 {

	private long lastId;

	public TimedIdSequence100() {
		this.lastId = System.currentTimeMillis() * 100;
	}

	public synchronized long newId() {
		long now = System.currentTimeMillis() * 100;
		if (now <= lastId) {
			now = ++lastId;
		} else {
			lastId = now;
		}
		return now;
	}
}