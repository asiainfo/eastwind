package eastwind.io3;

public class InvocationBuilder {

	private InvocationMode invocationMode = new InvocationMode();


	public InvocationBuilder transfer() {
		if (InvocationMode.TL.get() != invocationMode) {
			InvocationMode.TL.set(invocationMode);
		}
		return this;
	}
}
