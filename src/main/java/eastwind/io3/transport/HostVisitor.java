package eastwind.io3.transport;

import java.util.Iterator;

import eastwind.io3.obj.Host;

public interface HostVisitor extends Iterator<Host> {

	public void first();
}
