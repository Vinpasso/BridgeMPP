package bridgempp.service;

import java.util.Collection;
import java.util.logging.Level;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import bridgempp.Message;
import bridgempp.ShadowManager;
import bridgempp.data.DataManager;
import bridgempp.messageformat.MessageFormat;

@Entity(name = "SingleToMultiBridgeService")
public abstract class SingleToMultiBridgeService extends BridgeService
{
	@OneToMany(mappedBy = "service")
	private Collection<MultiBridgeServiceHandle<?>> handles;

	@Override
	public abstract void connect();

	@Override
	public abstract void disconnect();

	@Override
	public void sendMessage(Message message)
	{
		MultiBridgeServiceHandle<?> handle = getHandle(message.getDestination().getIdentifier());
		if(handle == null)
		{
			ShadowManager.log(Level.WARNING, "Attempted to send Message to non existent Handle: " + message.toString());
			return;
		}
		handle.sendMessage(message);
	}

	private MultiBridgeServiceHandle<?> getHandle(String identifier)
	{
		return DataManager.getFromPrimaryKey(MultiBridgeServiceHandle.class, identifier);
	}
	
	protected void addHandle(MultiBridgeServiceHandle<?> handle)
	{
		handles.add(handle);
	}

	@Override
	public abstract String getName();

	@Override
	public abstract boolean isPersistent();

	@Override
	public abstract MessageFormat[] getSupportedMessageFormats();

}
