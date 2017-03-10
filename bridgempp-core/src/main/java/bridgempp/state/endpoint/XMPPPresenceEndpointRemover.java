package bridgempp.state.endpoint;

import java.util.logging.Level;

import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.log.Log;
import bridgempp.service.BridgeService;
import bridgempp.services.xmpp.XMPPService;
import bridgempp.state.Event;
import bridgempp.state.EventListener;
import bridgempp.state.EventSubscribe;

@EventSubscribe({Event.BRIDGEMPP_SHUTDOWN})
public class XMPPPresenceEndpointRemover implements EventListener<BridgeService> {

	@Override
	public void onEvent(BridgeService service) {
		if(service instanceof XMPPService)
		{
			Endpoint xmppPresence = ((XMPPService)service).getXMPPPresenceEndpoint();
			//Clear User Database
			while(!xmppPresence.getUsers().isEmpty())
			{
				User user = xmppPresence.getUsers().iterator().next();
				Log.log(Level.INFO, "Removing User from XMPP Presence: " + user.toString());
				xmppPresence.removeUser(user);
				DataManager.deregisterUser(user);
			}
		}
	}

}
