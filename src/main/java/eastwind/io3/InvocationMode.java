package eastwind.io3;

import eastwind.io3.obj.Host;

public class InvocationMode {

	public static InvocationMode DEFAULT = new InvocationMode();

	static {
		DEFAULT.timeout = 10000;
		DEFAULT.retry = 1;
		DEFAULT.sync = true;
	}

	public static ThreadLocal<InvocationMode> TL = new ThreadLocal<InvocationMode>();

	private boolean sync;
	private int timeout;
	private Host host;
	private int retry;

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public Host getHost() {
		return host;
	}

	public void setHost(Host host) {
		this.host = host;
	}

	public boolean isSync() {
		return sync;
	}

	public void setSync(boolean sync) {
		this.sync = sync;
	}

	public int getRetry() {
		return retry;
	}

	public void setRetry(int retry) {
		this.retry = retry;
	}

}
