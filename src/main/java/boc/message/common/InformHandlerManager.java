package boc.message.common;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class InformHandlerManager {

	private ConcurrentMap<Integer, CopyOnWriteArrayList<InformHandler>> informHandlers = new ConcurrentHashMap<Integer, CopyOnWriteArrayList<InformHandler>>();

	public InformHandlerManager() {
		informHandlers.put(-1, new CopyOnWriteArrayList<InformHandler>());
	}

	public void addHandler(InformHandler imformHandler) {
		CopyOnWriteArrayList<InformHandler> handlers = informHandlers.get(imformHandler.type());
		if (handlers == null) {
			handlers = new CopyOnWriteArrayList<InformHandler>();
			CopyOnWriteArrayList<InformHandler> t = informHandlers.putIfAbsent(imformHandler.type(), handlers);
			if (t != null) {
				handlers = t;
			}
		}
		handlers.add(imformHandler);
	}

	public List<InformHandler> getHandlers(int type) {
		CopyOnWriteArrayList<InformHandler> handlers = informHandlers.get(type);
		if (handlers == null) {
			return Collections.emptyList();
		}
		return handlers;
	}
}
