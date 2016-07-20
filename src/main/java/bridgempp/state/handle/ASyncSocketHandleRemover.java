package bridgempp.state.handle;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;

import bridgempp.ShadowManager;
import bridgempp.data.DataManager;
import bridgempp.service.BridgeService;
import bridgempp.services.asyncsocket.ASyncSocketClient;
import bridgempp.services.asyncsocket.ASyncSocketService;
import bridgempp.state.EventListener;
import bridgempp.state.EventSubscribe;
import bridgempp.state.EventManager.Event;

@EventSubscribe({Event.SERVICE_CONNECTED, Event.SERVICE_DISCONNECTED})
public class ASyncSocketHandleRemover extends EventListener<BridgeService> {

	@Override
	public void onEvent(BridgeService eventMessage) {
		if(eventMessage.getClass().equals(ASyncSocketService.class))
		{
			Collection<ASyncSocketClient> collection = DataManager.list(ASyncSocketClient.class);
			Iterator<ASyncSocketClient> iterator = collection.iterator();
			while(iterator.hasNext())
			{
				ASyncSocketClient client = iterator.next();
				ShadowManager.log(Level.INFO, "Removing lingering handle: " + client.toString());
				client.disconnect();
			}
		}
	}

}

