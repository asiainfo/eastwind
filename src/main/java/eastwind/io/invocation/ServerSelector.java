package eastwind.io.invocation;

import java.util.Set;

import eastwind.io.transport.Node;

public interface ServerSelector {

	public HashCodeGenerator getHashCodeGenerator();
	
	public void prepare(NodeGroup nodeGroup);
	
	public Node next(int key, Set<Node> exculsions);
	
	public boolean exclusive();
	
	public boolean skippable();
	
}
