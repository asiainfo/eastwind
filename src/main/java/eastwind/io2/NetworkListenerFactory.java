package eastwind.io2;

public interface NetworkListenerFactory {

	Listener<Transport> getActiveListener();

	Listener<Shake> getShakeListener();

	Listener<NetworkTraffic> getPushListener();

}
