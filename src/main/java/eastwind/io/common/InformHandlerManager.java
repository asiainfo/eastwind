package eastwind.io.common;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class InformHandlerManager {

	private ConcurrentMap<Integer, CopyOnWriteArrayList<MessagingHandler>> messagingHandlers = new ConcurrentHashMap<Integer, CopyOnWriteArrayList<MessagingHandler>>();

	public InformHandlerManager() {
		messagingHandlers.put(-1, new CopyOnWriteArrayList<MessagingHandler>());
	}

	public void addHandler(MessagingHandler imformHandler) {
		CopyOnWriteArrayList<MessagingHandler> handlers = messagingHandlers.get(imformHandler.type());
		if (handlers == null) {
			handlers = new CopyOnWriteArrayList<MessagingHandler>();
			CopyOnWriteArrayList<MessagingHandler> t = messagingHandlers.putIfAbsent(imformHandler.type(), handlers);
			if (t != null) {
				handlers = t;
			}
		}
		handlers.add(imformHandler);
	}

	public List<MessagingHandler> getHandlers(int type) {
		CopyOnWriteArrayList<MessagingHandler> handlers = messagingHandlers.get(type);
		if (handlers == null) {
			return Collections.emptyList();
		}
		return handlers;
	}
}
