package bridgempp.service.stack.handle;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.persistence.MapKey;
import javax.persistence.OneToMany;

import bridgempp.Message;
import bridgempp.ShadowManager;
import bridgempp.data.DataManager;
import bridgempp.service.stack.simplex.DescendingStackElement;

public class ServiceHandleStackElement extends DescendingStackElement<Message>
{

	@OneToMany(mappedBy = "handleStackElement",targetEntity = MultiBridgeServiceHandle.class)
	@MapKey(name = "identifier")
	protected Map<String, MultiBridgeServiceHandle> handles = new HashMap<String, MultiBridgeServiceHandle>();

	
	@Override
	protected void messageDescending(Message input)
	{
		MultiBridgeServiceHandle handle = handles.get(input.getDestination().getIdentifier());
		if(handle == null)
		{
			ShadowManager.log(Level.WARNING, "Attempted to send Message to non existant Handle");
			return;
		}
		handle.sendMessage(input);
	}


	protected void addHandle(MultiBridgeServiceHandle handle)
	{
		DataManager.updateState(handle);
		handles.put(handle.identifier, handle);
	}
	
	protected void removeHandle(MultiBridgeServiceHandle handle)
	{
		handles.remove(handle.identifier);
		DataManager.removeState(handle);
	}

}
