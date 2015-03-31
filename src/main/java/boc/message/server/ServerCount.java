package boc.message.server;

import java.util.concurrent.atomic.AtomicInteger;

public class ServerCount {

	private volatile boolean shutdown;
	private AtomicInteger clientCount = new AtomicInteger();
	private AtomicInteger handlingCount = new AtomicInteger();
	
	public int getClientCount() {
		return clientCount.get();
	}

	public void incrementClientCount() {
		clientCount.getAndIncrement();
	}

	public void decrementClientCount() {
		clientCount.decrementAndGet();
	}

	public int getHandlingCount() {
		return handlingCount.intValue();
	}
	
	public void incrementHandlingCount() {
		handlingCount.getAndIncrement();
	}

	public void decrementHandlingCount() {
		handlingCount.decrementAndGet();
	}

	public boolean isShutdown() {
		return shutdown;
	}

	public void shutdown() {
		this.shutdown = true;
	}
}
