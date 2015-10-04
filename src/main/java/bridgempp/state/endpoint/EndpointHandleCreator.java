package bridgempp.state.endpoint;

import bridgempp.data.Endpoint;
import bridgempp.service.stack.handle.SingleToMultiBridgeService;
import bridgempp.state.EventListener;
import bridgempp.state.EventManager.Event;
import bridgempp.state.EventSubscribe;

@EventSubscribe(Event.ENDPOINT_CREATED)
public class EndpointHandleCreator extends EventListener<Endpoint>
{

	public void onEvent(Endpoint endpoint)
	{
		if(endpoint.getService() instanceof SingleToMultiBridgeService<?, ?>)
		{
			SingleToMultiBridgeService<?, ?> service = (SingleToMultiBridgeService<?, ?>) endpoint.getService();
			//TODO: Work in Progress: Automatic Handle creation
		}
	}
	
}
