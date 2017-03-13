package eastwind.io.transport;

import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eastwind.io.model.Host;

public class SimpleServerLoader extends ServerLoader {

	private Map<String, List<Node>> nodeGroup = Maps.newHashMap();
	
	@Override
	public List<Node> getNodes(String group) {
		return nodeGroup.get(group);
	}

	@Override
	public int getMod(String group) {
		return 0;
	}
	
	public void setHosts(String group, List<Host> hosts) {
		List<Node> nodes = Lists.transform(hosts, new Function<Host, Node>() {
			@Override
			public Node apply(Host input) {
				return new Node(input);
			}
		});
		nodeGroup.put(group, nodes);
	}
}
