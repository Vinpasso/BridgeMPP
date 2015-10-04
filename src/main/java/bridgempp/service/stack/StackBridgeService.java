package bridgempp.service.stack;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import bridgempp.Message;
import bridgempp.service.BridgeService;

@Entity(name = "Stack_Bridge_Service")
public abstract class StackBridgeService extends BridgeService
{
	@OneToOne(optional = false)
	protected ServiceStack stack = new ServiceStack();

	@Override
	public void sendMessage(Message message)
	{
		stack.sendMessage(message);
	}	
	
}
