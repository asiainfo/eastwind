package eastwind.io3.codex;

public class CodexHolder {

	private HandlerInitializer holder;

	public boolean isHolded() {
		return holder != null;
	}
	
	public void release() {
		this.holder = null;
	}

	public HandlerInitializer getHolder() {
		return holder;
	}

	public void setHolder(HandlerInitializer holder) {
		this.holder = holder;
	}
	
}
