package eastwind.io2;

public interface ListenerFactory {

	Listener<Transport> getActiveListener();

	Listener<Shake> getShakeListener();

	Listener<NetworkTraffic> getPushListener();

}
