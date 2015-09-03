package bridgempp.state;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import bridgempp.ShadowManager;
import bridgempp.state.EventManager.Event;

public class StateManager {
	private static HashMap<Event, ArrayList<EventListener<?>>> listeners = new HashMap<>();

	protected static void fireEvent(Event event, Object eventParameter) {
		ArrayList<EventListener<?>> objectEventListeners = listeners.get(event);
		for (int i = 0; i < objectEventListeners.size(); i++) {
			EventListener<?> eventListener = objectEventListeners.get(i);
			try {
				eventListener
						.getClass()
						.getMethod("onEvent",
								eventListener.getTypeOfEventMessage())
						.invoke(eventListener, event);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException
					| SecurityException e) {
				ShadowManager.log(Level.SEVERE,
						"An Error occured while attempting to launch Event", e);
			}
		}
	}

	public static void addListener(Event event, EventListener<?> listener) {
		listeners.get(event).add(listener);
	}

	public static void removeListener(Event event, EventListener<?> listener) {
		listeners.get(event).remove(listener);
	}

}
