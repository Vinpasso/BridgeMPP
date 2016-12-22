package bridgempp.state.endpoint;

import java.util.logging.Level;

import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.log.Log;
import bridgempp.state.Event;
import bridgempp.state.EventListener;
import bridgempp.state.EventSubscribe;

@EventSubscribe(Event.ENDPOINT_DISCONNECTED)
public class NonPersistantEndpointDisconnectedListener implements EventListener<Endpoint> {

	@Override
	public void onEvent(Endpoint endpoint) {
		if(!endpoint.getService().isPersistent())
		{
			while(!endpoint.getUsers().isEmpty())
			{
				User user = endpoint.getUsers().iterator().next();
				Log.log(Level.INFO, "Removing user due to Non Persistance: " + user);
				DataManager.deregisterUser(user);
			}
			Log.log(Level.INFO, "Removing endpoint due to Non Persistance: " + endpoint.toString());
			DataManager.deregisterEndpoint(endpoint);
		}
	}

}
