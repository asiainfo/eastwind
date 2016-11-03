package eastwind.io.invocation;

import java.util.List;

import eastwind.io.model.Host;
import eastwind.io.model.Unique;

public class Route implements Unique {

	private long id;
	private String consumerGroup;
	
	
	private String providerGroup;
	private String version;
	private List<Host> hosts;
	private int precedence;
	
	@Override
	public long getId() {
		return id;
	}

	@Override
	public void setId(long id) {
		this.id = id;
	}

	public int getPrecedence() {
		return precedence;
	}

	public void setPrecedence(int precedence) {
		this.precedence = precedence;
	}

	public List<Host> getHosts() {
		return hosts;
	}

	public void setHosts(List<Host> hosts) {
		this.hosts = hosts;
	}
}
