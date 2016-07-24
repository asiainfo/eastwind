package eastwind.io3;


public class TransportTicker implements DelayedListener<TransportPromise<?>> {

	public static final String NAME = "TRANSPORT_TICKER";
	
	@Override
	public String type() {
		return NAME;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void timeUp(final TransportPromise<?> tp, DelayedExecutor executor) {
		if (!tp.isDone()) {
			UniqueObject uo = createObject(tp);
			Transport t = tp.getTransport();
			TransportPromise<Integer> tickTp = t.publish(uo, null);
			tickTp.addListener(new OperationListener<ListenablePromise<Integer>>() {
				@Override
				public void complete(ListenablePromise<Integer> lp) {
					if (lp.isDone()) {
						if (lp.getNow() == 1) {
							tp.clearLosts();
						} else if (lp.getNow() == 0) {
							// resend
						} else if (lp.isCancelled()) {
							tp.lost();
						}
					}
				}
			});;
		}
		
	}

	private UniqueObject createObject(final TransportPromise<?> tp) {
		Handling h = new Handling();
		h.setId(tp.getId());
		UniqueObject uo = new UniqueObject();
		uo.setCall(true);
		uo.setObj(h);
		return uo;
	}

}
