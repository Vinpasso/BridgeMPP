package bridgempp.service;

import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import bridgempp.Message;
import bridgempp.ShadowManager;
import bridgempp.data.DataManager;
import bridgempp.messageformat.MessageFormat;

@Entity(name = "SingleToMultiBridgeService")
@DiscriminatorValue("SingleToMultiBridgeService")
public abstract class SingleToMultiBridgeService<S extends SingleToMultiBridgeService<S, H>, H extends MultiBridgeServiceHandle<S, H>> extends BridgeService
{
	@OneToMany(mappedBy = "service",targetEntity = MultiBridgeServiceHandle.class)
	protected Collection<H> handles = new LinkedList<H>();

	@Column(name = "Handle_Type")
	protected Class<H> handleClass;
	
	@Override
	public abstract void connect();

	@Override
	public abstract void disconnect();

	public SingleToMultiBridgeService(Class<H> handleClass)
	{
		this.handleClass = handleClass;
	}
	
	@Override
	public void sendMessage(Message message)
	{
		MultiBridgeServiceHandle<S, H> handle = getHandle(message.getDestination().getIdentifier());
		if(handle == null)
		{
			ShadowManager.log(Level.WARNING, "Attempted to send Message to non existent Handle: " + message.toString());
			return;
		}
		handle.sendMessage(message);
	}

	private H getHandle(String identifier)
	{
		return DataManager.getFromPrimaryKey(handleClass, identifier);
	}
	
	protected void addHandle(H handle)
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
