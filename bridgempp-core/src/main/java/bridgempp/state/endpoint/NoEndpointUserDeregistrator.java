package bridgempp.state.endpoint;

import java.util.logging.Level;

import bridgempp.ShadowManager;
import bridgempp.data.DataManager;
import bridgempp.data.User;
import bridgempp.state.EventListener;
import bridgempp.state.EventManager.Event;
import bridgempp.state.EventSubscribe;

@EventSubscribe({Event.BRIDGEMPP_STARTUP, Event.BRIDGEMPP_SHUTDOWN})
public class NoEndpointUserDeregistrator extends EventListener<Void> {

	@Override
	public void onEvent(Void eventMessage) {
		DataManager.list(User.class).forEach(user -> {
			if(user.getEndpoints().isEmpty())
			{
				ShadowManager.log(Level.INFO, "Deregistering User " + user.toString() + " due to lack of endpoint associations");
				DataManager.deregisterUser(user);
			}
		});
	}

}
