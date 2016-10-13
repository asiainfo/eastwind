package eastwind.io;



public class TransmitTicker {

//	@SuppressWarnings("unchecked")
//	@Override
//	public void timeUp(final TransmitPromise<?> tp, DelayedExecutor executor) {
//		if (!tp.isDone()) {
//			UniqueObject uo = createObject(tp);
//			Transport t = tp.getTransport();
//			TransmitPromise<Integer> tickTp = t.send(uo, null);
//			tickTp.addListener(new OperationListener<ListenablePromise<Integer>>() {
//				@Override
//				public void complete(ListenablePromise<Integer> lp) {
//					if (lp.isDone()) {
//						if (lp.getNow() == 1) {
//							tp.clearLosts();
//						} else if (lp.getNow() == 0) {
//							// resend
//						} else if (lp.isCancelled()) {
//							tp.lost();
//						}
//					}
//				}
//			});;
//		}
//		
//	}
//
//	private UniqueObject createObject(final TransmitPromise<?> tp) {
//		Handling h = new Handling();
//		h.setId(tp.getId());
//		UniqueObject uo = new UniqueObject();
//		uo.setCall(true);
//		uo.setObj(h);
//		return uo;
//	}

}
