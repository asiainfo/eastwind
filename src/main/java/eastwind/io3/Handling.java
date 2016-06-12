package eastwind.io3;

public class Handling implements Unique, FrameworkObject {

	private Long id;

	@Override
	public long getId() {
		return id;
	}

	@Override
	public void setId(long id) {
		this.id = id;
	}
	
}
