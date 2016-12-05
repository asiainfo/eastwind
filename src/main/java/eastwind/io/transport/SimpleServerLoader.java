package eastwind.io.transport;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

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
	
	public void setNodes(String group, List<Node> nodes) {
		nodeGroup.put(group, nodes);
	}
}
