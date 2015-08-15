package bridgempp.state;

import java.util.logging.Level;

import bridgempp.ShadowManager;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;

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
			while(!endpoint.getUsers().isEmpty())
			{
				User user = endpoint.getUsers().iterator().next();
				ShadowManager.log(Level.INFO, "Removing user due to Non Persistance: " + user);
				DataManager.deregisterUser(user);
			}
			ShadowManager.log(Level.INFO, "Removing endpoint due to Non Persistance: " + endpoint.toString());
			DataManager.deregisterEndpoint(endpoint);
		}
	}

	@Override
	public void removed(Endpoint endpoint) {

	}

}
