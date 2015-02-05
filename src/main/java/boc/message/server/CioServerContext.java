package boc.message.server;

import java.util.concurrent.atomic.AtomicInteger;

public class CioServerContext {

	private ProviderManager providerManager = new ProviderManager();

	private RequestPool requestPool = new RequestPool();

	private AtomicInteger clientCount = new AtomicInteger();

	public ProviderManager getProviderManager() {
		return providerManager;
	}

	public RequestPool getRequestPool() {
		return requestPool;
	}

	public int getClientCount() {
		return clientCount.get();
	}

	public void incrementClientCount() {
		clientCount.getAndIncrement();
	}

	public void decrementClientCount() {
		clientCount.decrementAndGet();
	}
}
