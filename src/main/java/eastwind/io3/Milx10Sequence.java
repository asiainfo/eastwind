package eastwind.io3;

public class Milx10Sequence {

	private long last;

	public Milx10Sequence() {
		this.last = System.currentTimeMillis() * 10;
	}

	public synchronized long create() {
		long now = System.currentTimeMillis() * 10;
		if (now <= last) {
			now = ++last;
		} else {
			last = now;
		}
		return now;
	}
}