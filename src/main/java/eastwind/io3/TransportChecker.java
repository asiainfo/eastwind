package eastwind.io3;


public class TransportChecker implements DelayedListener<TransportPromise<?>> {

	@SuppressWarnings("unchecked")
	@Override
	public void timeUp(final TransportPromise<?> tp, DelayedExecutor executor) {
		if (!tp.isDone()) {
			HandlingMessage m = new HandlingMessage();
			m.setId(tp.getId());
			UniqueObject uo = new UniqueObject();
			uo.setCall(true);
			uo.setObj(m);
			Transport trans = tp.getTransport();
			TransportPromise<Integer> checkTp = trans.publish(uo, null);
			checkTp.addListener(new OperationListener<ListenablePromise<Integer>>() {
				@Override
				public void operationComplete(ListenablePromise<Integer> lp) {
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
	
}
