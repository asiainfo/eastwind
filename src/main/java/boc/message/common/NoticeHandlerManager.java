package boc.message.common;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class NoticeHandlerManager {

	private ConcurrentMap<Integer, CopyOnWriteArrayList<NoticeHandler>> noticeHandlers = new ConcurrentHashMap<Integer, CopyOnWriteArrayList<NoticeHandler>>();

	public NoticeHandlerManager() {
		noticeHandlers.put(-1, new CopyOnWriteArrayList<NoticeHandler>());
	}

	public void addHandler(NoticeHandler noticeHandler) {
		CopyOnWriteArrayList<NoticeHandler> handlers = noticeHandlers.get(noticeHandler.type());
		if (handlers == null) {
			handlers = new CopyOnWriteArrayList<NoticeHandler>();
			CopyOnWriteArrayList<NoticeHandler> t = noticeHandlers.putIfAbsent(noticeHandler.type(), handlers);
			if (t != null) {
				handlers = t;
			}
		}
		handlers.add(noticeHandler);
	}

	public List<NoticeHandler> getHandlers(int type) {
		CopyOnWriteArrayList<NoticeHandler> handlers = noticeHandlers.get(type);
		if (handlers == null) {
			return Collections.emptyList();
		}
		return handlers;
	}
}
