package boc.message.server;

import boc.message.Session;

public interface SessionListener {

	public void created(Session session);

	public void destroyed(Session session);
}
