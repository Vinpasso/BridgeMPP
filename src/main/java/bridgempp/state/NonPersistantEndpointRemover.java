package bridgempp.state;

import java.util.logging.Level;

import bridgempp.ShadowManager;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;

public class NonPersistantEndpointRemover implements EndpointStateListener {

	@Override
	public void created(Endpoint endpoint) {
	}

	@Override
	public void connected(Endpoint endpoint) {
	}

	@Override
	public void disconnected(Endpoint endpoint) {
		if(!endpoint.getService().isPersistent())
		{
			ShadowManager.log(Level.INFO, "Removing endpoint due to Non Persistance");
			DataManager.deregisterEndpoint(endpoint);
		}
	}

	@Override
	public void removed(Endpoint endpoint) {

	}

}
