package bridgempp.service.stack;

import java.util.Collections;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import bridgempp.Message;
import bridgempp.service.BridgeService;

@Entity(name = "Service_Stack")
public class ServiceStack
{

	//Index 0 is Top of Stack
	//Top of Stack is highest abstraction (CommandInterpreterSender)
	@OneToMany(mappedBy = "stack")
	private List<ServiceStackLayer<?, ?>> stackLayers;
	
	@OneToOne(mappedBy = "stack")
	private StackBridgeService service;
	//TODO: Reverse side of Stack OneToOne
	
	
	public void sendMessage(Message message)
	{
		sendMessageFromTop(message);
	}

	protected void sendMessageFromTop(Message message)
	{
		if(stackLayers.isEmpty())
		{
			return;
		}
		ServiceStackLayer<Message, ?> serviceStackElement = (ServiceStackLayer<Message, ?>) stackLayers.get(0);
		serviceStackElement.messageDescending(message);
	}
	
	public List<ServiceStackLayer<?,?>> getStackLayers()
	{
		return Collections.unmodifiableList(stackLayers);
	}
	
	public void addLayer(ServiceStackLayer<?, ?> layer)
	{
		stackLayers.add(layer);
		if(stackLayers.size() > 1)
		{
			stackLayers.get(0).updateStackReferences(stackLayers.listIterator(1));
		}
	}

	public StackBridgeService getService()
	{
		return service;
	}
}
