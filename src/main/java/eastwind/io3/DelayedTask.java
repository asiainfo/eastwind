package eastwind.io3;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayedTask implements Delayed, Unique {
	long id;
	String type;
	Object obj;
	long exeTime;

	public DelayedTask(long id, String type, Object obj, long delay) {
		this.id = id;
		this.type = type;
		this.obj = obj;
		setExeTime(this, delay);
	}

	@Override
	public int compareTo(Delayed o) {
		if (o instanceof DelayedTask) {
			DelayedTask o1 = (DelayedTask) o;
			if (exeTime < o1.exeTime) {
				return -1;
			} else {
				return exeTime == o1.exeTime ? 0 : 1;
			}
		} else {
			long d1 = getDelay(TimeUnit.MILLISECONDS);
			long d2 = getDelay(TimeUnit.MILLISECONDS);
			if (d1 < d2) {
				return -1;
			} else {
				return d1 == d2 ? 0 : 1;
			}
		}
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(exeTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}

	public void setExeTime(DelayedTask dt, long delay) {
		dt.exeTime = System.currentTimeMillis() + delay;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public void setId(long id) {
		this.id = id;
	}
}
