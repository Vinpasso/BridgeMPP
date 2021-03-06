package bridgempp.state.endpoint;

import java.util.logging.Level;

import bridgempp.ShadowManager;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.service.BridgeService;
import bridgempp.services.xmpp.XMPPService;
import bridgempp.state.EventListener;
import bridgempp.state.EventSubscribe;
import bridgempp.state.EventManager.Event;

@EventSubscribe({Event.SERVICE_CONNECTED, Event.SERVICE_DISCONNECTED})
public class XMPPPresenceEndpointRemover extends EventListener<BridgeService> {

	@Override
	public void onEvent(BridgeService service) {
		if(service instanceof XMPPService)
		{
			Endpoint xmppPresence = ((XMPPService)service).getXMPPPresenceEndpoint();
			//Clear User Database
			while(!xmppPresence.getUsers().isEmpty())
			{
				User user = xmppPresence.getUsers().iterator().next();
				ShadowManager.log(Level.INFO, "Removing User from XMPP Presence: " + user.toString());
				xmppPresence.removeUser(user);
				DataManager.deregisterUser(user);
			}
		}
	}

}
