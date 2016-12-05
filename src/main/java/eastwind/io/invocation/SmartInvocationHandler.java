package eastwind.io.invocation;

import java.lang.reflect.Type;

import eastwind.io.model.ProviderMetaData;
import eastwind.io.support.SettableFuture;
import eastwind.io.transport.ServerRepository;
import eastwind.io.transport.ServerTransport;

public class SmartInvocationHandler extends AbstractInvocationHandler<String> {

	private Type returnType;

	public SmartInvocationHandler(String group, ServerRepository serverRepository, Type returnType) {
		super(group, serverRepository);
		this.returnType = returnType;
	}

	@Override
	protected SettableFuture<ProviderMetaData> getProviderMetaData(String context, ServerTransport st) {
		return st.getProviderMetaData(context);
	}

	@Override
	protected boolean isSmart(String context) {
		return false;
	}

	@Override
	protected Type getReturnType(String context) {
		return returnType;
	}

}
