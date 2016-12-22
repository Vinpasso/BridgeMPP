package bridgempp.state.endpoint;

import java.util.logging.Level;

import bridgempp.data.DataManager;
import bridgempp.data.User;
import bridgempp.log.Log;
import bridgempp.state.Event;
import bridgempp.state.EventListener;
import bridgempp.state.EventSubscribe;

@EventSubscribe({Event.BRIDGEMPP_STARTUP, Event.BRIDGEMPP_SHUTDOWN})
public class NoEndpointUserDeregistrator implements EventListener<Void> {

	@Override
	public void onEvent(Void eventMessage) {
		DataManager.list(User.class).forEach(user -> {
			if(user.getEndpoints().isEmpty())
			{
				Log.log(Level.INFO, "Deregistering User " + user.toString() + " due to lack of endpoint associations");
				DataManager.deregisterUser(user);
			}
		});
	}

}
