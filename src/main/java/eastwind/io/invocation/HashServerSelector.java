package eastwind.io.invocation;

import java.util.Set;

import eastwind.io.transport.Node;

public class HashServerSelector implements ServerSelector {

	private NodeGroup nodeGroup;
	
	@Override
	public void prepare(NodeGroup nodeGroup) {
		this.nodeGroup = nodeGroup;
	}

	@Override
	public Node next(int key, Set<Node> exculsions) {
		return nodeGroup.nextWithExculsions(key, exculsions);
	}

	@Override
	public boolean exclusive() {
		return true;
	}

	@Override
	public boolean skippable() {
		return true;
	}

	@Override
	public HashCodeGenerator getHashCodeGenerator() {
		return RandomHashCodeGenerator.DEFAULT;
	}

}