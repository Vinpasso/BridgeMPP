package bridgempp.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;

import bridgempp.ShadowManager;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.processing.Schedule;
import bridgempp.message.DeliveryGoal;
import bridgempp.message.Message;

@Entity(name = "SINGLETOMULTIBRIDGESERVICE")
@DiscriminatorValue("SingleToMultiBridgeService")
public abstract class SingleToMultiBridgeService<S extends SingleToMultiBridgeService<S, H>, H extends MultiBridgeServiceHandle<S, H>> extends BridgeService
{
	@OneToMany(mappedBy = "service", targetEntity = MultiBridgeServiceHandle.class)
	@MapKey(name = "identifier")
	protected Map<String, H> handles = new HashMap<String, H>();

	@Override
	public abstract void connect();

	@Override
	public void disconnect()
	{
		if (!isPersistent())
		{
			Iterator<H> iterator = handles.values().iterator();
			while (iterator.hasNext())
			{
				H handle = iterator.next();
				iterator.remove();
				DataManager.removeState(handle);
			}
		}
	}

	@Override
	public void sendMessage(Message message, DeliveryGoal deliveryGoal)
	{
		Endpoint endpoint = deliveryGoal.getTarget();
		MultiBridgeServiceHandle<S, H> handle = getHandle(endpoint.getIdentifier());
		if (handle == null)
		{
			ShadowManager.log(Level.WARNING, "Attempted to send Message to non existent Handle (Delaying endpoint removal): " + message.toString());
			Schedule.schedule(() -> {
				ShadowManager.log(Level.INFO, "Removal of Endpoint " + endpoint.toString() + " imminent, searching for handle");
				H handle2 = getHandle(endpoint.getIdentifier());
				if(handle2 != null)
				{
					ShadowManager.log(Level.INFO, "Found a handle, removing it.");
					removeHandle(handle2);
					ShadowManager.log(Level.INFO, "Successfully removed handle");
				}
				ShadowManager.log(Level.INFO, "Attempting to remove endpoint");
				DataManager.deregisterEndpointAndUsers(endpoint);
			});
			return;
		}
		handle.sendMessage(message);
	}

	private H getHandle(String identifier)
	{
		return handles.get(identifier);
	}

	protected void addHandle(H handle)
	{
		DataManager.updateState(handle);
		handles.put(handle.identifier, handle);
	}

	protected void removeHandle(H handle)
	{
		handles.remove(handle.identifier);
		DataManager.removeState(handle);
		while (!handle.endpoint.getUsers().isEmpty())
		{
			DataManager.deregisterUser(handle.endpoint.getUsers().iterator().next());
		}
		DataManager.deregisterEndpoint(handle.endpoint);
	}

	@Override
	public abstract String getName();

	@Override
	public abstract boolean isPersistent();

}
