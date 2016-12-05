package eastwind.io.invocation;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Joiner;

import eastwind.io.model.Host;
import eastwind.io.model.Unique;

public class Route implements Unique {

	public static final int WHEN = 0;
	public static final int ONLY = 1;

	private long id;
	private String name;

	private int model;
	private boolean exclusive;
	private boolean skippable;

	private String consumer;
	private String fromVersion;
	private String expression;
	private List<String> fromIps;

	private String provider;
	private String toVersion;
	private int rate;
	private Set<Host> toHosts;

	private int precedence;
	private Date time = new Date();

	@Override
	public long getId() {
		return id;
	}

	@Override
	public void setId(long id) {
		this.id = id;
	}

	public int getModel() {
		return model;
	}

	public void setModel(int model) {
		this.model = model;
	}

	public int getPrecedence() {
		return precedence;
	}

	public void setPrecedence(int precedence) {
		this.precedence = precedence;
	}

	public String getConsumer() {
		return consumer;
	}

	public void setConsumer(String consumer) {
		this.consumer = consumer;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getFromVersion() {
		return fromVersion;
	}

	public void setFromVersion(String fromVersion) {
		this.fromVersion = fromVersion;
	}

	public String getToVersion() {
		return toVersion;
	}

	public void setToVersion(String toVersion) {
		this.toVersion = toVersion;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public List<String> getFromIps() {
		return fromIps;
	}

	public void setFromIps(List<String> fromIps) {
		this.fromIps = fromIps;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Host> getToHosts() {
		return toHosts;
	}

	public void setToHosts(Set<Host> toHosts) {
		this.toHosts = toHosts;
	}

	public boolean isExclusive() {
		return exclusive;
	}

	public void setExclusive(boolean exclusive) {
		this.exclusive = exclusive;
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public boolean isSkippable() {
		return skippable;
	}

	public void setSkippable(boolean skippable) {
		this.skippable = skippable;
	}

}
