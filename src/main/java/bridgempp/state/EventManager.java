package bridgempp.state;

import org.hibernate.AnnotationException;

import bridgempp.state.endpoint.NonPersistantEndpointDisconnectedListener;

public class EventManager {

	public static <T> void loadEventListenerClass(EventListener<?> eventListener)
	{
		EventSubscribe annotation = eventListener.getClass().getAnnotation(EventSubscribe.class);
		if(annotation == null)
		{
			throw new AnnotationException("Event Listener is missing Event Subscribe Annotation: " + eventListener.getClass().getName());
		}
		for(Event event : annotation.value())
		{
			StateManager.addListener(event, eventListener);
		}
	}
	
	public static <T> void fireEvent(Event event, Object eventParameter)
	{
		StateManager.fireEvent(event, eventParameter);
	}
	
	public static void loadCentralEventSubscribers()
	{
		StateManager.initializeEventSystem();
		loadEventListenerClass(new NonPersistantEndpointDisconnectedListener());
	}
	
	public enum Event
	{
		ENDPOINT_CREATED,
		ENDPOINT_CONNECTED,
		ENDPOINT_DISCONNECTED,
		ENDPOINT_REMOVED
	}
}
