package bridgempp.state.handle;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;

import bridgempp.data.DataManager;
import bridgempp.log.Log;
import bridgempp.service.BridgeService;
import bridgempp.services.asyncsocket.ASyncSocketClient;
import bridgempp.services.asyncsocket.ASyncSocketService;
import bridgempp.state.Event;
import bridgempp.state.EventListener;
import bridgempp.state.EventSubscribe;

@EventSubscribe({Event.SERVICE_CONNECTED, Event.SERVICE_DISCONNECTED})
public class ASyncSocketHandleRemover implements EventListener<BridgeService> {

	@Override
	public void onEvent(BridgeService service) {
		if(service instanceof ASyncSocketService)
		{
			Collection<ASyncSocketClient> collection = DataManager.list(ASyncSocketClient.class);
			Iterator<ASyncSocketClient> iterator = collection.iterator();
			while(iterator.hasNext())
			{
				ASyncSocketClient client = iterator.next();
				Log.log(Level.INFO, "Removing lingering handle: " + client.toString());
				client.disconnect();
			}
		}
	}

}

