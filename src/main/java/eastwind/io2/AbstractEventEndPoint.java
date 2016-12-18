package eastwind.io2;

import java.net.InetSocketAddress;

import com.google.common.eventbus.EventBus;

public abstract class AbstractEventEndPoint extends AbstractEndPoint implements EventEndPoint {

	protected EventBus eventBus;
	
	protected AbstractEventEndPoint(String uuid, String group, String tag, String version, int weight) {
		super(uuid, group, tag, version, weight);
	}
	
	protected void initEventBus(String tag, InetSocketAddress address) {
		if (eventBus == null) {
			StringBuilder sb = new StringBuilder();
			sb.append(tag).append("-");
			sb.append(group).append("-");
			sb.append(uuid);
			eventBus = new EventBus(sb.toString());
		}
	}
	
	@Override
	public void register(Object listener) {
		eventBus.register(listener);
	}

	@Override
	public void post(Object event) {
		eventBus.post(event);
	}

}
