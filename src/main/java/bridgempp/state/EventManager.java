package bridgempp.state;

import bridgempp.state.endpoint.NonPersistantEndpointDisconnectedListener;

public class EventManager {

	public static <T> void loadEventListenerClass(Event event, EventListener<?> eventListener)
	{
		StateManager.addListener(event, eventListener);
	}
	
	public static <T> void fireEvent(Event event, Object eventParameter)
	{
		StateManager.fireEvent(event, eventParameter);
	}
	
	public static void loadCentralEventSubscribers()
	{
		loadEventListenerClass(Event.ENDPOINT_DISCONNECTED, new NonPersistantEndpointDisconnectedListener());
	}
	
	public enum Event
	{
		ENDPOINT_CREATED,
		ENDPOINT_CONNECTED,
		ENDPOINT_DISCONNECTED,
		ENDPOINT_REMOVED
	}
}
