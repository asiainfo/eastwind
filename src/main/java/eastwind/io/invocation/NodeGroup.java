package eastwind.io.invocation;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import eastwind.io.model.Host;
import eastwind.io.transport.Node;

public class NodeGroup {

	private Set<Node> nodes;
	private TreeMap<Long, Node> map;

	public void init(Set<Node> nodes) {
		HashFunction hf = Hashing.crc32();
		Charset charset = Charset.forName("utf-8");
		TreeMap<Long, Node> map = new TreeMap<Long, Node>();
		
		int weights = 0;
		for (Node node : nodes) {
			weights += node.getWeight();
		}
		int perWeight = 512 / weights;
		
		for (Node node : nodes) {
			int virtual = node.getWeight() * perWeight;
			for (int i = 0; i < virtual; i++) {
				Hasher hasher = hf.newHasher();
				if (node.getTag() != null) {
					hasher.putString(node.getTag(), charset);
				}

				Host host = node.getHost();
				hasher.putString(host.getIp(), charset);
				hasher.putInt(host.getPort());
				hasher.putInt(i);
				map.put(hasher.hash().asInt() & 0xffffffffl, node);
			}
		}
		
		this.map = map;
		this.nodes = new HashSet<Node>(nodes);
	}

	public Node nextWithExculsions(int key, Set<Node> exculsions) {
		long k = key & 0xffffffffl;
		for (Node n : map.tailMap(k).values()) {
			if (exculsions == null || !exculsions.contains(n)) {
				return n;
			}
		}
		k = 0;
		for (Node n : map.values()) {
			if (exculsions == null || !exculsions.contains(n)) {
				return n;
			}
		}
		return null;
	}

	public Node nextWithInculsions(int key, Set<Node> inculsions) {
		long k = key & 0xffffffffl;
		for (Node n : map.tailMap(k).values()) {
			if (inculsions.contains(n)) {
				return n;
			}
		}
		k = 0;
		for (Node n : map.values()) {
			if (inculsions.contains(n)) {
				return n;
			}
		}
		return null;
	}

	public Set<Node> getNodes() {
		return Collections.unmodifiableSet(nodes);
	}
}
