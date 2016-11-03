package eastwind.io.invocation;

import java.lang.reflect.Type;

import eastwind.io.ServerConfigurer;
import eastwind.io.model.HandlerMetaData;
import eastwind.io.support.SettableFuture;
import eastwind.io.transport.ServerRepository;
import eastwind.io.transport.ServerTransport;

public class SmartInvocationHandler extends AbstractInvocationHandler<String> {

	private Type returnType;
	
	public SmartInvocationHandler(String group, ServerRepository serverRepository, ServerConfigurer serverConfigurer,
			Type returnType) {
		super(group, serverRepository, serverConfigurer);
		this.returnType = returnType;
	}

	@Override
	protected SettableFuture<HandlerMetaData> getHandlerMetaData(String context, ServerTransport st) {
		return st.getHandlerMetaData(context);
	}

	@Override
	protected boolean isBinary(String context) {
		return false;
	}

	@Override
	protected Type getReturnType(String context) {
		return returnType;
	}

}
