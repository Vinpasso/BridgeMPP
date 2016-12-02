package bridgempp.state.endpoint;

import java.util.logging.Level;

import bridgempp.ShadowManager;
import bridgempp.data.DataManager;
import bridgempp.data.TimedEndpoint;
import bridgempp.state.EventListener;
import bridgempp.state.EventManager.Event;
import bridgempp.state.EventSubscribe;

@EventSubscribe({Event.BRIDGEMPP_STARTUP, Event.BRIDGEMPP_SHUTDOWN})
public class TimedEndpointExpiryCheck extends EventListener<Void> {

	@Override
	public void onEvent(Void eventMessage) {
		try {
			DataManager.acquireDOMWritePermission();
			ShadowManager.log(Level.INFO, "Checking for expired endpoints...");
			DataManager.list(TimedEndpoint.class).forEach(endpoint -> endpoint.checkExpired());
			ShadowManager.log(Level.INFO, "Finished checking for expired endpoints.");
		} catch (InterruptedException e) {
			ShadowManager.log(Level.INFO, "Failed to check for expired endpoints.", e);
		}
		DataManager.releaseDOMWritePermission();

	}
	
}
