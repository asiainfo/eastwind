package eastwind.websocket;

import io.netty.handler.codec.http.QueryStringDecoder;

public interface Upgrader {

	public Upgrade upgrade(QueryStringDecoder decoder);
	
}
