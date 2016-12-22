package bridgempp.state.endpoint;

import java.util.logging.Level;

import bridgempp.data.DataManager;
import bridgempp.data.TimedEndpoint;
import bridgempp.log.Log;
import bridgempp.state.Event;
import bridgempp.state.EventListener;
import bridgempp.state.EventSubscribe;

@EventSubscribe({ Event.BRIDGEMPP_STARTUP, Event.BRIDGEMPP_SHUTDOWN })
public class TimedEndpointExpiryCheck implements EventListener<Void>
{

	@Override
	public void onEvent(Void eventMessage)
	{
		try
		{
			DataManager.acquireDOMWritePermission();
			Log.log(Level.INFO, "Checking for expired endpoints...");
			DataManager.list(TimedEndpoint.class).forEach(endpoint -> {
				if (endpoint.checkExpired())
				{
					Log.log(Level.INFO, "Endpoint " + this.toString() + " has expired, removing...");
					DataManager.removeState(this);
				}
			});
			Log.log(Level.INFO, "Finished checking for expired endpoints.");
		} catch (InterruptedException e)
		{
			Log.log(Level.INFO, "Failed to check for expired endpoints.", e);
		}
		DataManager.releaseDOMWritePermission();

	}

}
