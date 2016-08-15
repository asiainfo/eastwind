package eastwind.io3;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import eastwind.io3.RemoteAppGroup.HostTraverser;

public class RemoteInvocationHandler implements InvocationHandler {
	
	private RemoteAppGroup remoteAppGroup;
	
	public RemoteInvocationHandler(RemoteAppGroup remoteAppGroup) {
		this.remoteAppGroup = remoteAppGroup;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		HostTraverser hostTraverser = remoteAppGroup.hostTraverser();
		DefaultHostSelector selector = new DefaultHostSelector(hostTraverser);
		
		return null;
	}

}
