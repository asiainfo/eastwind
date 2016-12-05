package eastwind.io.transport;

import java.lang.reflect.Method;

import eastwind.io.model.ProviderMetaData;
import eastwind.io.support.SettableFuture;

public interface ProviderMetaDataVisitor {

	public SettableFuture<ProviderMetaData> get(Method method);

	public SettableFuture<ProviderMetaData> putIfAbsent(Method method, SettableFuture<ProviderMetaData> future);

	public SettableFuture<ProviderMetaData> get(String name);

	public SettableFuture<ProviderMetaData> putIfAbsent(String name, SettableFuture<ProviderMetaData> future);
}
