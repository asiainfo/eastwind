package eastwind.io2;

import eastwind.io.Provider;

@Provider("")
public interface InternalProvider {

	ProviderSign sign(ProviderSign desc);

}