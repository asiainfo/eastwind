package eastwind.io3;

public class MillisX10Sequence implements Sequence {

	private long last;

	public MillisX10Sequence() {
		this.last = System.currentTimeMillis() * 10;
	}

	public synchronized long get() {
		long now = System.currentTimeMillis() * 10;
		if (now <= last) {
			now = ++last;
		} else {
			last = now;
		}
		return now;
	}
}