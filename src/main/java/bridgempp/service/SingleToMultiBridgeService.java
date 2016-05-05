package bridgempp.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import bridgempp.Message;
import bridgempp.ShadowManager;
import bridgempp.data.DataManager;

@Entity(name = "SingleToMultiBridgeService")
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
	public void sendMessage(Message message)
	{
		MultiBridgeServiceHandle<S, H> handle = getHandle(message.getDestination().getIdentifier());
		if (handle == null)
		{
			ShadowManager.log(Level.WARNING, "Attempted to send Message to non existent Handle: " + message.toString());
			DataManager.deregisterEndpointAndUsers(message.getDestination());
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
