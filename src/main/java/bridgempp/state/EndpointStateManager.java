package bridgempp.state;

import java.util.ArrayList;
import bridgempp.data.Endpoint;

public class EndpointStateManager {

	private static ArrayList<EndpointStateListener> listeners = new ArrayList<>();
	
	public static void created(Endpoint endpoint)
	{
		for(EndpointStateListener listener : listeners)
		{
			listener.created(endpoint);
		}
	}
	
	public static void connected(Endpoint endpoint)
	{
		for(EndpointStateListener listener : listeners)
		{
			listener.connected(endpoint);
		}
	}
	
	public static void disconnected(Endpoint endpoint)
	{
		for(EndpointStateListener listener : listeners)
		{
			listener.disconnected(endpoint);
		}
	}
	
	public static void removed(Endpoint endpoint)
	{
		for(EndpointStateListener listener : listeners)
		{
			listener.removed(endpoint);
		}
	}

	
	public static void addEndpointStateListener(EndpointStateListener listener)
	{
		if(listeners.contains(listener))
		{
			return;
		}
		listeners.add(listener);
	}
	
	public static void removeEndpointStateListener(EndpointStateListener listener)
	{
		listeners.remove(listener);
	}
}
